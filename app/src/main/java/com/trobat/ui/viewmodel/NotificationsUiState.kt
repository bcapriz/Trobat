package com.trobat.ui.viewmodel

import com.trobat.data.local.NotificationEntity
import com.trobat.data.model.CitizenReport

data class NotificationsUiState(
    val alerts: List<NotificationEntity> = emptyList(),
    val reports: List<CitizenReport> = emptyList(),
    val unreadCount: Int = 0
)
