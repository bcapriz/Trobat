package com.trobat.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.trobat.data.local.PendingReportDao
import com.trobat.data.local.PendingReportEntity
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

private const val TAG = "RemoteCitizenReportRepo"
private const val TWO_DAYS_MS = 2L * 24 * 60 * 60 * 1000

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

    override suspend fun sendReport(report: CitizenReport, photoUri: Uri?, localFilePath: String?): Boolean {
        val localPhotoPath = copyPhotoToInternalStorage(report.id, photoUri, localFilePath)

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

        val sent = trySendToApi(report, localPhotoPath, photoUri)
        if (sent) {
            markSent(report.id)
            localPhotoPath?.let { File(it).delete() }
        }
        return sent
    }

    override suspend fun retrySyncPending() {
        if (!syncMutex.tryLock()) return
        try {
            val pending = pendingReportDao.getAll().filter { it.status == ReportStatus.PENDING_SYNC.name }
            for (entity in pending) {
                pendingReportDao.updateStatus(entity.id, ReportStatus.SENDING.name)
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
                if (trySendToApi(report, entity.localPhotoPath, null)) {
                    markSent(entity.id)
                    entity.localPhotoPath?.let { File(it).delete() }
                } else {
                    pendingReportDao.updateStatus(entity.id, ReportStatus.PENDING_SYNC.name)
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
        pendingReportDao.deleteSentOlderThan(System.currentTimeMillis() - TWO_DAYS_MS)
    }

    private suspend fun markSent(id: String) {
        pendingReportDao.getById(id)?.let { entity ->
            pendingReportDao.insert(entity.copy(status = ReportStatus.SENT.name, localPhotoPath = null))
        }
    }

    private suspend fun trySendToApi(report: CitizenReport, localPhotoPath: String?, photoUri: Uri?): Boolean {
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
            val file = File(path)
            if (file.exists()) {
                try {
                    MultipartBody.Part.createFormData(
                        name = "foto",
                        filename = "photo.jpg",
                        body = file.readBytes().toRequestBody("image/jpeg".toMediaType())
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to read local photo file", e)
                    null
                }
            } else null
        } ?: photoUri?.let { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bytes = stream.readBytes()
                    MultipartBody.Part.createFormData(
                        name = "foto",
                        filename = "photo.jpg",
                        body = bytes.toRequestBody("image/jpeg".toMediaType())
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to read photo from URI", e)
                null
            }
        }

        return try {
            api.crearReporte(fotoPart, datosPart).isSuccessful
        } catch (e: Exception) {
            Log.w(TAG, "API call failed", e)
            false
        }
    }

    private fun copyPhotoToInternalStorage(reportId: String, photoUri: Uri?, localFilePath: String?): String? {
        val dir = File(context.filesDir, "pending_reports").also { it.mkdirs() }
        val dest = File(dir, "$reportId.jpg")
        return try {
            when {
                localFilePath != null && File(localFilePath).exists() -> {
                    File(localFilePath).copyTo(dest, overwrite = true)
                    dest.absolutePath
                }
                photoUri != null -> {
                    val copied = context.contentResolver.openInputStream(photoUri)?.use { input ->
                        dest.outputStream().use { output -> input.copyTo(output) }
                    }
                    if (copied != null) dest.absolutePath else null
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to copy photo to internal storage", e)
            null
        }
    }
}
