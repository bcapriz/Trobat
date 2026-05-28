package com.trobat.ui.viewmodel

import com.trobat.data.model.CitizenReport
import com.trobat.data.model.MissingPersonCase

data class CitizenHomeUiState(
    val title: String = "¿Cómo querés colaborar?",
    val subtitle: String = "Tu mirada puede ayudar a otros.",
    val activeCases: List<MissingPersonCase> = emptyList(),
    val nearbyReports: List<CitizenReport> = emptyList(),
    val unreadNotifications: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val totalReports: Int get() = nearbyReports.size
    val totalCases: Int get() = activeCases.size
    val latestReport: CitizenReport? get() = nearbyReports.lastOrNull()
}
