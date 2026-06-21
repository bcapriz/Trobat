package com.trobat.data.repository.mapper

import com.trobat.data.remote.dto.CasoDto
import com.trobat.data.model.MissingPersonCase

fun CasoDto.toDomain(): MissingPersonCase {
    val coords = missing_person.last_known_location?.coordinates
    val lat = coords?.getOrNull(1) ?: 0.0
    val lng = coords?.getOrNull(0) ?: 0.0
    val location = (missing_person.location_label ?: missing_person.location_description).ifBlank {
        if (lat != 0.0) "Lat: ${"%.5f".format(lat)}, Lng: ${"%.5f".format(lng)}" else ""
    }
    return MissingPersonCase(
        id = id,
        fullName = missing_person.name,
        age = missing_person.age,
        physicalDescription = missing_person.description,
        lastSeenLocation = location,
        lastSeenDate = missing_person.last_seen_date.ifBlank { created_at },
        area = status,
        latitude = lat,
        longitude = lng
    )
}
