package com.trobat.data.repository.mapper

import com.trobat.data.remote.dto.CasoDto
import com.trobat.data.model.MissingPersonCase

fun CasoDto.toDomain(): MissingPersonCase {
    val coords = desaparecido.ultima_ubicacion_oficial?.coordinates
    val lat = coords?.getOrNull(1) ?: 0.0
    val lng = coords?.getOrNull(0) ?: 0.0
    val location = desaparecido.descripcion_ubicacion.ifBlank {
        if (lat != 0.0) "Lat: ${"%.5f".format(lat)}, Lng: ${"%.5f".format(lng)}" else ""
    }
    return MissingPersonCase(
        id = id,
        fullName = desaparecido.nombre,
        age = desaparecido.edad,
        physicalDescription = desaparecido.descripcion,
        lastSeenLocation = location,
        lastSeenDate = fecha_creacion,
        area = estado,
        latitude = lat,
        longitude = lng
    )
}
