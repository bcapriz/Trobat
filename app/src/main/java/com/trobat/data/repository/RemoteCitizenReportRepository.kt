package com.trobat.data.repository

import android.content.Context
import com.google.gson.Gson
import com.trobat.data.model.CapturedEvidenceHolder
import com.trobat.data.model.CitizenReport
import com.trobat.data.model.ReportStatus
import com.trobat.data.remote.TrobatApi
import com.trobat.data.remote.dto.ContactInfoDto
import com.trobat.data.remote.dto.CrearReporteRequestDto
import com.trobat.data.remote.dto.SecurityMetadataDto
import com.trobat.data.remote.dto.UbicacionDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class RemoteCitizenReportRepository(
    private val api: TrobatApi,
    private val context: Context
) : CitizenReportRepository {

    private val gson = Gson()
    private val _reports = MutableStateFlow<List<CitizenReport>>(emptyList())
    override val reports: StateFlow<List<CitizenReport>> = _reports.asStateFlow()

    override fun getNearbyReports(): List<CitizenReport> = _reports.value

    override fun getUnreadNotificationsCount(): Int =
        _reports.value.count { it.status == ReportStatus.NEW }

    override suspend fun sendReport(report: CitizenReport) {
        val datos = CrearReporteRequestDto(
            case_id = report.caseId,
            location = UbicacionDto(
                type = "Point",
                coordinates = listOf(report.longitude, report.latitude)
            ),
            description = report.description,
            police_priority = false,
            security_metadata = SecurityMetadataDto(anonymous = report.isAnonymous),
            contact_info = ContactInfoDto()
        )

        val datosJson = gson.toJson(datos)
        val datosPart = datosJson.toRequestBody("application/json".toMediaType())

        val fotoPart = CapturedEvidenceHolder.photoUri?.let { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bytes = stream.readBytes()
                    MultipartBody.Part.createFormData(
                        name = "foto",
                        filename = "photo.jpg",
                        body = bytes.toRequestBody("image/jpeg".toMediaType())
                    )
                }
            } catch (_: Exception) {
                null
            }
        }

        try {
            val response = api.crearReporte(fotoPart, datosPart)
            if (response.isSuccessful) {
                val sent = report.copy(status = ReportStatus.SENT)
                _reports.value = listOf(sent) + _reports.value
            }
        } catch (_: Exception) {
        }
    }
}
