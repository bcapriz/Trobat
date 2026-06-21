package com.trobat.ui.viewmodel

import com.trobat.data.model.MissingPersonCase
import com.trobat.utils.GeoUtils

data class HeatMapUiState(
    val cases: List<MissingPersonCase> = emptyList(),
    val totalCases: Int = 0,
    val mostActiveArea: String = "-",
    val isLoading: Boolean = true,
    val userLat: Double? = null,
    val userLng: Double? = null,
    val radiusKm: Float = 10f
) {
    val filteredCases: List<MissingPersonCase> get() =
        if (userLat != null && userLng != null) {
            cases
                .filter { GeoUtils.haversineKm(userLat, userLng, it.latitude, it.longitude) <= radiusKm }
                .sortedBy { GeoUtils.haversineKm(userLat, userLng, it.latitude, it.longitude) }
        } else {
            cases
        }

    fun distanceTo(case: MissingPersonCase): Double? {
        if (userLat == null || userLng == null) return null
        return GeoUtils.haversineKm(userLat, userLng, case.latitude, case.longitude)
    }
}
