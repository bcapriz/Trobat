package com.trobat.data.repository

import com.trobat.data.model.CitizenReport
import com.trobat.data.model.ReportStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCitizenReportRepository : CitizenReportRepository {

    private val initialReports = listOf(
        CitizenReport(
            id = "1",
            caseId = "1",
            title = "Nuevo reporte cercano",
            description = "Posible avistamiento registrado cerca de una plaza.",
            optionalDetails = "La persona caminaba hacia Av. Santa Fe.",
            address = "Av. Siempre Viva, CABA",
            createdAt = "Hoy, 16:32",
            latitude = -34.6037,
            longitude = -58.3816,
            status = ReportStatus.NEW
        ),
        CitizenReport(
            id = "2",
            caseId = "2",
            title = "Nueva evidencia",
            description = "Se recibió una foto desde la cámara de la app.",
            optionalDetails = null,
            address = "Judah Centro, CABA",
            createdAt = "Hoy, 15:30",
            latitude = -34.6042,
            longitude = -58.3821,
            status = ReportStatus.SENT
        )
    )

    private val _reports = MutableStateFlow(initialReports)

    override val reports: StateFlow<List<CitizenReport>> =
        _reports.asStateFlow()

    override fun getNearbyReports(): List<CitizenReport> {
        return _reports.value
    }

    override fun getUnreadNotificationsCount(): Int {
        return _reports.value.count { report ->
            report.status == ReportStatus.NEW
        }
    }

    override fun sendReport(report: CitizenReport) {
        _reports.value = _reports.value + report
    }
}