package com.trobat.data.repository

import com.trobat.data.model.CitizenReport
import kotlinx.coroutines.flow.StateFlow

interface CitizenReportRepository {

    val reports: StateFlow<List<CitizenReport>>

    fun getNearbyReports(): List<CitizenReport>

    fun getUnreadNotificationsCount(): Int

    suspend fun sendReport(report: CitizenReport)
}