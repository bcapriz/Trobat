package com.trobat.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest {

    @Test
    fun `valid ISO string returns formatted Spanish date`() {
        val result = formatLastSeenDate("2024-05-24T21:30:00Z")
        // UTC-3 (Argentina): 21:30 UTC = 18:30 local
        assertTrue("Expected '24 de mayo de 2024' in '$result'", result.contains("24 de mayo de 2024"))
    }

    @Test
    fun `time offset applied correctly for Argentina UTC-3`() {
        // UTC midnight = 21:00 previous day in Argentina
        val result = formatLastSeenDate("2024-01-15T03:00:00Z")
        // 03:00 UTC = 00:00 Argentina
        assertTrue("Expected '00:00' in '$result'", result.contains("00:00"))
    }

    @Test
    fun `invalid string returns input unchanged`() {
        val input = "not-a-date"
        assertEquals(input, formatLastSeenDate(input))
    }

    @Test
    fun `empty string returns empty string`() {
        assertEquals("", formatLastSeenDate(""))
    }

    @Test
    fun `result contains day of MMMM de yyyy pattern`() {
        val result = formatLastSeenDate("2024-12-01T12:00:00Z")
        // Should match "1 de diciembre de 2024" (or similar day)
        assertTrue("Expected 'de' separators in '$result'", result.contains(" de "))
    }

    @Test
    fun `partially valid string returns input unchanged`() {
        val input = "2024-13-99" // invalid month/day
        assertEquals(input, formatLastSeenDate(input))
    }

    @Test
    fun `time portion appears in output`() {
        val result = formatLastSeenDate("2024-06-15T16:00:00Z") // 13:00 Argentina
        assertTrue("Expected time in '$result'", result.contains("13:00"))
    }
}
