package com.trobat.data.remote.dto

data class MetadataSeguridadDto(
    val anonimo: Boolean = true
)

data class DatosContactoDto(
    val nombre: String? = null,
    val telefono: String? = null,
    val email: String? = null
)

data class CrearReporteRequestDto(
    val caso_id: String,
    val location: UbicacionDto,
    val descripcion: String,
    val prioridad_policial: Boolean = false,
    val metadata_seguridad: MetadataSeguridadDto = MetadataSeguridadDto(),
    val datos_contacto: DatosContactoDto = DatosContactoDto()
)

data class ReporteDto(
    val id: String,
    val caso_id: String,
    val location: UbicacionDto = UbicacionDto(),
    val timestamp: String = "",
    val prioridad_policial: Boolean = false,
    val descripcion: String = "",
    val photo_url: String? = null,
    val metadata_seguridad: MetadataSeguridadDto = MetadataSeguridadDto(),
    val datos_contacto: DatosContactoDto = DatosContactoDto(),
    val validado: Boolean = false
)

data class ReportesPaginadosDto(
    val data: List<ReporteDto>,
    val total: Long,
    val page: Int,
    val limit: Int,
    val hasMore: Boolean
)
