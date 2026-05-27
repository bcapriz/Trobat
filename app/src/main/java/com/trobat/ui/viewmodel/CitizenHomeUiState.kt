package com.trobat.ui.viewmodel

import com.trobat.data.model.CitizenReport

data class CitizenHomeUiState(
    val title: String = "¿Cómo querés colaborar?",
    val subtitle: String = "Tu mirada puede ayudar a otros.",
    val nearbyReports: List<CitizenReport> = emptyList(),
    val unreadNotifications: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val totalReports: Int
        get() = nearbyReports.size

    val latestReport: CitizenReport?
        get() = nearbyReports.lastOrNull()
}