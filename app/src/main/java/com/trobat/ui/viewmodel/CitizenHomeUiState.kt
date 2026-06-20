package com.trobat.ui.viewmodel

import com.trobat.domain.model.MissingPersonCase

data class CitizenHomeUiState(
    val title: String = "¿Cómo querés colaborar?",
    val activeCases: List<MissingPersonCase> = emptyList(),
    val searchQuery: String = "",
    val expandedCaseId: String? = null
) {
    val filteredCases: List<MissingPersonCase> get() =
        if (searchQuery.isBlank()) activeCases
        else activeCases.filter { case ->
            case.fullName.contains(searchQuery, ignoreCase = true) ||
            case.lastSeenLocation.contains(searchQuery, ignoreCase = true) ||
            case.area.contains(searchQuery, ignoreCase = true)
        }
}
