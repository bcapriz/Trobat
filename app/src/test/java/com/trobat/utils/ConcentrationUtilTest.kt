package com.trobat.utils

import com.trobat.data.model.MissingPersonCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConcentrationUtilTest {

    private fun case(id: String, area: String, lat: Double = 0.0, lng: Double = 0.0) =
        MissingPersonCase(
            id = id, fullName = "Test", age = 30, physicalDescription = "",
            lastSeenLocation = "", lastSeenDate = "", area = area,
            latitude = lat, longitude = lng
        )

    @Test
    fun `empty list returns null`() {
        assertNull(ConcentrationUtil.mostConcentrated(emptyList()))
    }

    @Test
    fun `single case returns that area`() {
        val cases = listOf(case("1", "Lanús"))
        val result = ConcentrationUtil.mostConcentrated(cases)
        assertEquals("Lanús", result?.label)
        assertEquals(1, result?.count)
        assertEquals(1, result?.total)
    }

    @Test
    fun `returns most common area by name`() {
        val cases = listOf(
            case("1", "Wilde"),
            case("2", "Wilde"),
            case("3", "Quilmes"),
        )
        val result = ConcentrationUtil.mostConcentrated(cases)
        assertEquals("Wilde", result?.label)
        assertEquals(2, result?.count)
        assertEquals(3, result?.total)
    }

    @Test
    fun `total includes all cases regardless of area`() {
        val cases = listOf(
            case("1", "Wilde"),
            case("2", "Wilde"),
            case("3", "Quilmes"),
            case("4", "Lanús"),
        )
        val result = ConcentrationUtil.mostConcentrated(cases)
        assertEquals(4, result?.total)
    }

    @Test
    fun `summary format is correct`() {
        val cases = listOf(case("1", "Wilde"), case("2", "Wilde"))
        val result = ConcentrationUtil.mostConcentrated(cases)
        assertEquals("Wilde • 2 de 2 casos", result?.summary)
    }

    @Test
    fun `falls back to geographic grid when all areas are blank`() {
        // Two cases in same ~1km grid cell, one different
        val cases = listOf(
            case("1", "", lat = -34.000, lng = -58.000),
            case("2", "", lat = -34.001, lng = -58.001), // same cell
            case("3", "", lat = -35.500, lng = -58.000), // different cell
        )
        val result = ConcentrationUtil.mostConcentrated(cases)
        assertNotNull(result)
        assertEquals(2, result!!.count)
        assertEquals(3, result.total)
        assertTrue(result.label.isNotBlank())
    }

    @Test
    fun `area grouping takes priority over geographic grid`() {
        // Cases with area defined — should NOT fall through to grid
        val cases = listOf(
            case("1", "Wilde", lat = -34.000, lng = -58.000),
            case("2", "Quilmes", lat = -34.001, lng = -58.001), // close geo but different area
        )
        val result = ConcentrationUtil.mostConcentrated(cases)
        // Both areas have count=1, so any area is returned but NOT geographic grouping
        assertNotNull(result)
        assertTrue(result!!.count == 1)
    }

    @Test
    fun `all cases in same area returns count equal to total`() {
        val cases = listOf(
            case("1", "Wilde"),
            case("2", "Wilde"),
            case("3", "Wilde"),
        )
        val result = ConcentrationUtil.mostConcentrated(cases)
        assertEquals(3, result?.count)
        assertEquals(3, result?.total)
        assertEquals("Wilde • 3 de 3 casos", result?.summary)
    }

    @Test
    fun `geographic fallback returns null when all cases have zero coords and no area`() {
        // All at 0.0,0.0 → filtered out by withCoords filter → returns null
        val cases = listOf(
            case("1", ""),
            case("2", ""),
        )
        val result = ConcentrationUtil.mostConcentrated(cases)
        assertNull(result)
    }
}
