package com.trobat.utils

import kotlin.math.*

object GeoUtils {

    fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        return 6371.0 * 2 * asin(sqrt(a))
    }

    fun formatDistance(km: Double): String =
        if (km < 1.0) "${(km * 1000).roundToInt()} m"
        else "${"%.1f".format(km)} km"
}
