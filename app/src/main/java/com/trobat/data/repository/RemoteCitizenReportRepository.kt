package com.trobat.data.repository

import android.content.Context
import com.google.gson.Gson
import com.trobat.data.local.PendingReportDao
import com.trobat.data.local.PendingReportEntity
import com.trobat.data.model.CapturedEvidenceHolder
import com.trobat.data.model.CitizenReport
import com.trobat.data.model.ReportStatus
import com.trobat.data.remote.TrobatApi
import com.trobat.data.remote.dto.ContactInfoDto
import com.trobat.data.remote.dto.CrearReporteRequestDto
import com.trobat.data.remote.dto.SecurityMetadataDto
import com.trobat.data.remote.dto.UbicacionDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RemoteCitizenReportRepository(
    private val api: TrobatApi,
    private val context: Context,
    private val pendingReportDao: PendingReportDao,
    private val authRepository: AuthRepository
) : CitizenReportRepository {

    private val gson = Gson()
    private val syncMutex = Mutex()
    private val _reports = MutableStateFlow<List<CitizenReport>>(emptyList())
    override val reports: StateFlow<List<CitizenReport>> = _reports.asStateFlow()
    override val pendingReports: Flow<List<PendingReportEntity>> = pendingReportDao.observeAll()

    override fun getNearbyReports(): List<CitizenReport> = _reports.value

    override fun getUnreadNotificationsCount(): Int =
        _reports.value.count { it.status == ReportStatus.NEW }

    override suspend fun sendReport(report: CitizenReport): Boolean {
        val localPhotoPath = copyPhotoToInternalStorage(report.id)

        pendingReportDao.insert(
            PendingReportEntity(
                id = report.id,
                caseId = report.caseId,
                description = report.description,
                optionalDetails = report.optionalDetails,
                address = report.address,
                createdAt = report.createdAt,
                latitude = report.latitude,
                longitude = report.longitude,
                isAnonymous = report.isAnonymous,
                contactName = report.contactName,
                contactPhone = report.contactPhone,
                contactEmail = report.contactEmail,
                localPhotoPath = localPhotoPath
            )
        )

        val sent = trySendToApi(report, localPhotoPath)
        if (sent) {
            markSent(report.id)
            localPhotoPath?.let { File(it).delete() }
        }
        return sent
    }

    override suspend fun retrySyncPending() {
        if (!syncMutex.tryLock()) return
        try {
            val pending = pendingReportDao.getAll().filter { it.status == "PENDING_SYNC" }
            for (entity in pending) {
                pendingReportDao.updateStatus(entity.id, "SENDING")
                val report = CitizenReport(
                    id = entity.id,
                    caseId = entity.caseId,
                    title = "Reporte ciudadano",
                    description = entity.description,
                    optionalDetails = entity.optionalDetails,
                    address = entity.address,
                    createdAt = entity.createdAt,
                    latitude = entity.latitude,
                    longitude = entity.longitude,
                    isAnonymous = entity.isAnonymous,
                    contactName = entity.contactName,
                    contactPhone = entity.contactPhone,
                    contactEmail = entity.contactEmail,
                    status = ReportStatus.PENDING_SYNC
                )
                if (trySendToApi(report, entity.localPhotoPath)) {
                    markSent(entity.id)
                    entity.localPhotoPath?.let { File(it).delete() }
                } else {
                    pendingReportDao.updateStatus(entity.id, "PENDING_SYNC")
                }
            }
        } finally {
            syncMutex.unlock()
        }
    }

    override suspend fun resetStuckSending() {
        pendingReportDao.resetSendingToPending()
    }

    override suspend fun cleanupSentReports() {
        val twoDaysAgo = System.currentTimeMillis() - 2L * 24 * 60 * 60 * 1000
        pendingReportDao.deleteSentOlderThan(twoDaysAgo)
    }

    private suspend fun markSent(id: String) {
        pendingReportDao.getById(id)?.let { entity ->
            pendingReportDao.insert(entity.copy(status = "SENT", localPhotoPath = null))
        }
    }

    private suspend fun trySendToApi(report: CitizenReport, localPhotoPath: String?): Boolean {
        val contactInfo = if (!report.isAnonymous) {
            ContactInfoDto(
                name = report.contactName ?: authRepository.getUserName(),
                phone = report.contactPhone ?: authRepository.getPhone(),
                email = report.contactEmail ?: authRepository.getEmail()
            )
        } else {
            ContactInfoDto()
        }

        val datos = CrearReporteRequestDto(
            case_id = report.caseId,
            location = UbicacionDto(
                type = "Point",
                coordinates = listOf(report.longitude, report.latitude)
            ),
            description = report.description,
            police_priority = false,
            security_metadata = SecurityMetadataDto(anonymous = report.isAnonymous),
            contact_info = contactInfo
        )
        val datosPart = gson.toJson(datos).toRequestBody("application/json".toMediaType())

        val fotoPart = localPhotoPath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    MultipartBody.Part.createFormData(
                        name = "foto",
                        filename = "photo.jpg",
                        body = file.readBytes().toRequestBody("image/jpeg".toMediaType())
                    )
                } else null
            } catch (_: Exception) { null }
        } ?: CapturedEvidenceHolder.photoUri?.let { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bytes = stream.readBytes()
                    MultipartBody.Part.createFormData(
                        name = "foto",
                        filename = "photo.jpg",
                        body = bytes.toRequestBody("image/jpeg".toMediaType())
                    )
                }
            } catch (_: Exception) { null }
        }

        return try {
            api.crearReporte(fotoPart, datosPart).isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    private fun copyPhotoToInternalStorage(reportId: String): String? {
        val uri = CapturedEvidenceHolder.photoUri ?: return null
        return try {
            val dir = File(context.filesDir, "pending_reports").also { it.mkdirs() }
            val dest = File(dir, "$reportId.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
            dest.absolutePath
        } catch (_: Exception) {
            null
        }
    }
}
