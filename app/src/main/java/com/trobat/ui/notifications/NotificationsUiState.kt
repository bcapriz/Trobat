package com.trobat.ui.notifications

import com.trobat.data.local.db.entity.NotificationEntity

data class NotificationsUiState(
    val alerts: List<NotificationEntity> = emptyList(),
    val pendingReports: List<PendingReportItem> = emptyList(),
    val unreadCount: Int = 0
)
