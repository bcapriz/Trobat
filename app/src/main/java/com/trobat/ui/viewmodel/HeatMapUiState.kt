package com.trobat.ui.viewmodel

import com.trobat.data.model.MissingPersonCase
import com.trobat.utils.GeoUtils

data class HeatMapUiState(
    val cases: List<MissingPersonCase> = emptyList(),
    val totalCases: Int = 0,
    val mostActiveArea: String = "-",
    val isLoading: Boolean = true,
    val expandedCaseId: String? = null,
    val userLat: Double? = null,
    val userLng: Double? = null,
    val radiusKm: Float = 50f
) {
    val filteredCases: List<MissingPersonCase> get() {
        val lat = userLat ?: return cases
        val lng = userLng ?: return cases
        val (withCoords, withoutCoords) = cases.partition { it.latitude != 0.0 || it.longitude != 0.0 }
        val nearby = withCoords
            .filter { GeoUtils.haversineKm(lat, lng, it.latitude, it.longitude) <= radiusKm }
            .sortedBy { GeoUtils.haversineKm(lat, lng, it.latitude, it.longitude) }
        return nearby + withoutCoords
    }

    fun distanceTo(case: MissingPersonCase): Double? {
        val lat = userLat ?: return null
        val lng = userLng ?: return null
        return GeoUtils.haversineKm(lat, lng, case.latitude, case.longitude)
    }
}
