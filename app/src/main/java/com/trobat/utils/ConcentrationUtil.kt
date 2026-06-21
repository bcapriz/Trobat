package com.trobat.utils

import com.trobat.data.model.MissingPersonCase

object ConcentrationUtil {

    data class Result(val label: String, val count: Int, val total: Int) {
        val summary: String get() = "$label • $count de $total casos"
    }

    fun mostConcentrated(cases: List<MissingPersonCase>): Result? {
        if (cases.isEmpty()) return null

        // Primero intentamos agrupar por nombre de área (si hay áreas definidas)
        val byArea = cases
            .filter { it.area.isNotBlank() }
            .groupingBy { it.area }
            .eachCount()

        if (byArea.isNotEmpty()) {
            val (area, count) = byArea.maxByOrNull { it.value } ?: return null
            return Result(area, count, cases.size)
        }

        // Fallback: clustering geográfico por celda de ~1 km
        val withCoords = cases.filter { it.latitude != 0.0 || it.longitude != 0.0 }
        if (withCoords.isEmpty()) return null

        val byGrid = withCoords.groupingBy { gridCell(it.latitude, it.longitude) }.eachCount()
        val (_, count) = byGrid.maxByOrNull { it.value } ?: return null
        return Result("zona geográfica", count, cases.size)
    }

    // Resolución ~1.1 km en latitud, ~0.9 km en longitud (a -34°)
    private fun gridCell(lat: Double, lng: Double): Pair<Int, Int> =
        Pair((lat * 100).toInt(), (lng * 100).toInt())
}
