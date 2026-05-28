package com.trobat.ui.viewmodel

import com.trobat.data.model.CitizenReport

data class NotificationsUiState(
    val reports: List<CitizenReport> = emptyList(),
    val unreadCount: Int = 0
)
