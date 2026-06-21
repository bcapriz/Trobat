package com.trobat.ui.viewmodel

import com.trobat.data.model.MissingPersonCase
import com.trobat.utils.GeoUtils

data class HeatMapUiState(
    val cases: List<MissingPersonCase> = emptyList(),
    val totalCases: Int = 0,
    val mostActiveArea: String = "-",
    val mostActiveCount: Int = 0,
    val isLoading: Boolean = true,
    val expandedCaseId: String? = null,
    val userLat: Double? = null,
    val userLng: Double? = null,
    val radiusKm: Float = 50f
) {
    val filteredCases: List<MissingPersonCase> get() {
        val lat = userLat ?: return cases
        val lng = userLng ?: return cases
        return GeoUtils.filterAndSortByProximity(cases, lat, lng, radiusKm)
    }

    fun distanceTo(case: MissingPersonCase): Double? {
        val lat = userLat ?: return null
        val lng = userLng ?: return null
        return GeoUtils.haversineKm(lat, lng, case.latitude, case.longitude)
    }
}
