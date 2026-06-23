package com.trobat.data.local

import android.content.Context

class LastLocationPrefs(context: Context) {

    private val prefs = context.getSharedPreferences("last_location", Context.MODE_PRIVATE)

    fun save(lat: Double, lng: Double) {
        prefs.edit()
            .putLong("lat", java.lang.Double.doubleToRawLongBits(lat))
            .putLong("lng", java.lang.Double.doubleToRawLongBits(lng))
            .apply()
    }

    fun load(): Pair<Double, Double>? {
        if (!prefs.contains("lat")) return null
        val lat = java.lang.Double.longBitsToDouble(prefs.getLong("lat", 0L))
        val lng = java.lang.Double.longBitsToDouble(prefs.getLong("lng", 0L))
        return Pair(lat, lng)
    }
}
