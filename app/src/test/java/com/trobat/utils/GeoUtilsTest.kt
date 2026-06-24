package com.trobat.utils

import com.trobat.data.model.MissingPersonCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class GeoUtilsTest {

    private fun case(id: String, lat: Double, lng: Double, area: String = "") = MissingPersonCase(
        id = id, fullName = "Test", age = 30, physicalDescription = "",
        lastSeenLocation = "", lastSeenDate = "", area = area,
        latitude = lat, longitude = lng
    )

    // haversineKm

    @Test
    fun `haversine same point returns zero`() {
        assertEquals(0.0, GeoUtils.haversineKm(-34.0, -58.0, -34.0, -58.0), 0.001)
    }

    @Test
    fun `haversine Buenos Aires to Cordoba is approximately 645 km`() {
        val d = GeoUtils.haversineKm(-34.6037, -58.3816, -31.4201, -64.1888)
        assertTrue("Expected ~645 km, got $d", abs(d - 645.0) < 15.0)
    }

    @Test
    fun `haversine is symmetric`() {
        val d1 = GeoUtils.haversineKm(-34.0, -58.0, -31.0, -64.0)
        val d2 = GeoUtils.haversineKm(-31.0, -64.0, -34.0, -58.0)
        assertEquals(d1, d2, 0.001)
    }

    @Test
    fun `haversine one degree latitude is approximately 111 km`() {
        val d = GeoUtils.haversineKm(-34.0, -58.0, -35.0, -58.0)
        assertTrue("Expected ~111 km, got $d", abs(d - 111.0) < 5.0)
    }

    // filterAndSortByProximity

    @Test
    fun `filterAndSortByProximity returns only cases within radius`() {
        val close = case("1", -34.00, -58.0)
        val far = case("2", -35.50, -58.0) // ~167 km
        val result = GeoUtils.filterAndSortByProximity(
            listOf(close, far), userLat = -34.0, userLng = -58.0, radiusKm = 50f
        )
        assertEquals(listOf(close), result)
    }

    @Test
    fun `filterAndSortByProximity sorts by distance ascending`() {
        val nearest = case("1", -34.01, -58.0)
        val middle = case("2", -34.05, -58.0)
        val result = GeoUtils.filterAndSortByProximity(
            listOf(middle, nearest), userLat = -34.0, userLng = -58.0, radiusKm = 20f
        )
        assertEquals(listOf(nearest, middle), result)
    }

    @Test
    fun `filterAndSortByProximity places zero-coord cases at end`() {
        val withCoords = case("1", -34.01, -58.0)
        val noCoords = case("2", 0.0, 0.0)
        val result = GeoUtils.filterAndSortByProximity(
            listOf(noCoords, withCoords), userLat = -34.0, userLng = -58.0, radiusKm = 20f
        )
        assertEquals("1", result.first().id)
        assertEquals("2", result.last().id)
    }

    @Test
    fun `filterAndSortByProximity returns empty when all beyond radius`() {
        val far = case("1", -40.0, -58.0)
        val result = GeoUtils.filterAndSortByProximity(
            listOf(far), userLat = -34.0, userLng = -58.0, radiusKm = 10f
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterAndSortByProximity with empty list returns empty`() {
        val result = GeoUtils.filterAndSortByProximity(
            emptyList(), userLat = -34.0, userLng = -58.0, radiusKm = 50f
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterAndSortByProximity includes case exactly at radius boundary`() {
        // ~111 km at 1 degree latitude delta
        val atBoundary = case("1", -35.0, -58.0)
        val result = GeoUtils.filterAndSortByProximity(
            listOf(atBoundary), userLat = -34.0, userLng = -58.0, radiusKm = 115f
        )
        assertEquals(1, result.size)
    }

    // formatDistance

    @Test
    fun `formatDistance under 1km returns meters`() {
        assertEquals("500 m", GeoUtils.formatDistance(0.5))
    }

    @Test
    fun `formatDistance exactly 0 returns 0 m`() {
        assertEquals("0 m", GeoUtils.formatDistance(0.0))
    }

    @Test
    fun `formatDistance exactly 1km returns km format`() {
        assertEquals("1.0 km", GeoUtils.formatDistance(1.0))
    }

    @Test
    fun `formatDistance above 1km formats to one decimal`() {
        assertEquals("12.3 km", GeoUtils.formatDistance(12.345))
    }

    @Test
    fun `formatDistance rounds meters correctly`() {
        assertEquals("750 m", GeoUtils.formatDistance(0.75))
    }
}
