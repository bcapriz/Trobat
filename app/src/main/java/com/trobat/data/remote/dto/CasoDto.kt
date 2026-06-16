package com.trobat.data.remote.dto

data class UbicacionDto(
    val type: String = "Point",
    val coordinates: List<Double> = emptyList()
)

data class DesaparecidoDto(
    val nombre: String = "",
    val descripcion: String = "",
    val edad: Int = 0,
    val fecha_ultima_vez_visto: String = "",
    val descripcion_ubicacion: String = "",
    val ultima_ubicacion_oficial: UbicacionDto? = null
)

data class RepresentanteExternoDto(
    val nombre: String = "",
    val email: String = "",
    val telefono: String = ""
)

data class CasoDto(
    val id: String,
    val oficial_administrador_id: String,
    val agentes_asignados: List<String> = emptyList(),
    val desaparecido: DesaparecidoDto = DesaparecidoDto(),
    val representante_externo: RepresentanteExternoDto = RepresentanteExternoDto(),
    val estado: String = "",
    val total_reportes: Int = 0,
    val fecha_creacion: String = ""
)

data class CasosPaginadosDto(
    val data: List<CasoDto>,
    val total: Long,
    val page: Int,
    val limit: Int,
    val hasMore: Boolean
)
