package com.trobat.ui.viewmodel

import com.trobat.data.model.MissingPersonCase
import com.trobat.utils.GeoUtils

data class CitizenHomeUiState(
    val title: String = "¿Cómo querés colaborar?",
    val activeCases: List<MissingPersonCase> = emptyList(),
    val searchQuery: String = "",
    val expandedCaseId: String? = null,
    val userLat: Double? = null,
    val userLng: Double? = null,
    val radiusKm: Float = 50f,
    val isLoading: Boolean = true
) {
    val filteredCases: List<MissingPersonCase> get() {
        val bySearch = if (searchQuery.isBlank()) activeCases
        else activeCases.filter { case ->
            case.fullName.contains(searchQuery, ignoreCase = true) ||
            case.lastSeenLocation.contains(searchQuery, ignoreCase = true) ||
            case.area.contains(searchQuery, ignoreCase = true)
        }
        val lat = userLat ?: return bySearch
        val lng = userLng ?: return bySearch
        return GeoUtils.filterAndSortByProximity(bySearch, lat, lng, radiusKm)
    }

    fun distanceTo(case: MissingPersonCase): Double? {
        val lat = userLat ?: return null
        val lng = userLng ?: return null
        return GeoUtils.haversineKm(lat, lng, case.latitude, case.longitude)
    }
}
