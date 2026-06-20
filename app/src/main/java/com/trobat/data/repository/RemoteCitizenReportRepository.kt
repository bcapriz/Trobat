package com.trobat.data.repository

import android.content.Context
import com.google.gson.Gson
import com.trobat.data.model.CapturedEvidenceHolder
import com.trobat.domain.model.CitizenReport
import com.trobat.domain.model.ReportStatus
import com.trobat.data.remote.TrobatApi
import com.trobat.data.remote.dto.CrearReporteRequestDto
import com.trobat.data.remote.dto.DatosContactoDto
import com.trobat.data.remote.dto.MetadataSeguridadDto
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

    private val _reports = MutableStateFlow<List<CitizenReport>>(emptyList())
    override val reports: StateFlow<List<CitizenReport>> = _reports.asStateFlow()

    override fun getNearbyReports(): List<CitizenReport> = _reports.value

    override fun getUnreadNotificationsCount(): Int =
        _reports.value.count { it.status == ReportStatus.NEW }

    override suspend fun sendReport(report: CitizenReport) {
        val datos = CrearReporteRequestDto(
            caso_id = report.caseId,
            location = UbicacionDto(
                type = "Point",
                coordinates = listOf(report.longitude, report.latitude)
            ),
            descripcion = report.description,
            prioridad_policial = false,
            metadata_seguridad = MetadataSeguridadDto(anonimo = report.optionalDetails == null),
            datos_contacto = DatosContactoDto()
        )

        val datosJson = Gson().toJson(datos)
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
