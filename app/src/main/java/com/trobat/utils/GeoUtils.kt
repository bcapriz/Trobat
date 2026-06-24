package com.trobat.utils

import com.trobat.data.model.MissingPersonCase
import kotlin.math.*

object GeoUtils {

    fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        return 6371.0 * 2 * asin(sqrt(a))
    }

    fun filterAndSortByProximity(
        cases: List<MissingPersonCase>,
        userLat: Double,
        userLng: Double,
        radiusKm: Float
    ): List<MissingPersonCase> {
        val (withCoords, withoutCoords) = cases.partition { it.latitude != 0.0 || it.longitude != 0.0 }
        val nearby = withCoords
            .map { it to haversineKm(userLat, userLng, it.latitude, it.longitude) }
            .filter { (_, d) -> d <= radiusKm }
            .sortedBy { (_, d) -> d }
            .map { (case, _) -> case }
        return nearby + withoutCoords
    }

    fun formatDistance(km: Double): String =
        if (km < 1.0) "${(km * 1000).roundToInt()} m"
        else "${"%.1f".format(km)} km"
}
