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
    val radiusKm: Float = 10f
) {
    val filteredCases: List<MissingPersonCase> get() {
        val bySearch = if (searchQuery.isBlank()) activeCases
        else activeCases.filter { case ->
            case.fullName.contains(searchQuery, ignoreCase = true) ||
            case.lastSeenLocation.contains(searchQuery, ignoreCase = true) ||
            case.area.contains(searchQuery, ignoreCase = true)
        }
        return if (userLat != null && userLng != null) {
            bySearch
                .filter { GeoUtils.haversineKm(userLat, userLng, it.latitude, it.longitude) <= radiusKm }
                .sortedBy { GeoUtils.haversineKm(userLat, userLng, it.latitude, it.longitude) }
        } else {
            bySearch
        }
    }

    fun distanceTo(case: MissingPersonCase): Double? {
        if (userLat == null || userLng == null) return null
        return GeoUtils.haversineKm(userLat, userLng, case.latitude, case.longitude)
    }
}
