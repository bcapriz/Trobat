package com.trobat.ui.viewmodel

import com.trobat.data.local.NotificationEntity
import com.trobat.data.local.PendingReportEntity

data class NotificationsUiState(
    val alerts: List<NotificationEntity> = emptyList(),
    val pendingReports: List<PendingReportEntity> = emptyList(),
    val unreadCount: Int = 0
)
