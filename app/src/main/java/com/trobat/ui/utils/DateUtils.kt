package com.trobat.ui.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatLastSeenDate(isoString: String): String {
    return try {
        val instant = Instant.parse(isoString)
        val zdt = instant.atZone(ZoneId.of("America/Argentina/Buenos_Aires"))
        val formatter = DateTimeFormatter.ofPattern(
            "d 'de' MMMM 'de' yyyy, HH:mm",
            Locale.forLanguageTag("es-AR")
        )
        zdt.format(formatter)
    } catch (_: Exception) {
        isoString
    }
}
