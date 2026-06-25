package com.trobat.data.repository

import com.trobat.data.local.db.entity.PendingReportEntity
import com.trobat.data.model.CitizenReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CitizenReportRepository {

    val reports: StateFlow<List<CitizenReport>>

    val pendingReports: Flow<List<PendingReportEntity>>

    fun getNearbyReports(): List<CitizenReport>

    fun getUnreadNotificationsCount(): Int

    suspend fun sendReport(report: CitizenReport, photoUri: android.net.Uri?, localFilePath: String?): Boolean

    suspend fun retrySyncPending()

    suspend fun resetStuckSending()

    suspend fun cleanupSentReports()
}