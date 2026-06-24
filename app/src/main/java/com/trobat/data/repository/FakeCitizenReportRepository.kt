package com.trobat.data.repository

import com.trobat.data.local.PendingReportEntity
import com.trobat.data.model.CitizenReport
import com.trobat.data.model.ReportStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeCitizenReportRepository : CitizenReportRepository {

    private val initialReports = listOf(
        CitizenReport(
            id = "1",
            caseId = "1",
            title = "Nuevo reporte cercano",
            description = "Posible avistamiento registrado cerca de una plaza.",
            optionalDetails = "La persona caminaba por Avenida Mitre hacia el centro de Avellaneda.",
            address = "Av. Mitre y Las Flores, Wilde",
            createdAt = "Hoy, 16:32",
            latitude = -34.7011,
            longitude = -58.3261,
            status = ReportStatus.NEW
        ),
        CitizenReport(
            id = "2",
            caseId = "2",
            title = "Nueva evidencia",
            description = "Se recibió una foto desde la cámara de la app.",
            optionalDetails = null,
            address = "Retiro, CABA",
            createdAt = "Hoy, 15:30",
            latitude = -34.6042,
            longitude = -58.3821,
            status = ReportStatus.SENT
        )
    )

    private val _reports = MutableStateFlow(initialReports)

    override val reports: StateFlow<List<CitizenReport>> = _reports.asStateFlow()
    override val pendingReports: Flow<List<PendingReportEntity>> = flowOf(emptyList())

    override fun getNearbyReports(): List<CitizenReport> {
        return _reports.value
    }

    override fun getUnreadNotificationsCount(): Int {
        return _reports.value.count { report ->
            report.status == ReportStatus.NEW
        }
    }

    override suspend fun sendReport(report: CitizenReport, photoUri: android.net.Uri?, localFilePath: String?): Boolean {
        _reports.value = _reports.value + report
        return true
    }

    override suspend fun retrySyncPending() {}

    override suspend fun resetStuckSending() {}

    override suspend fun cleanupSentReports() {}
}