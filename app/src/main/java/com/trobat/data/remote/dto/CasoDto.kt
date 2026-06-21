package com.trobat.data.remote.dto

data class UbicacionDto(
    val type: String = "Point",
    val coordinates: List<Double> = emptyList()
)

data class DesaparecidoDto(
    val name: String = "",
    val description: String = "",
    val age: Int = 0,
    val image: String = "",
    val last_seen_date: String = "",
    val location_description: String = "",
    val last_known_location: UbicacionDto? = null,
    val location_label: String? = null
)

data class ExternalContactDto(
    val name: String = "",
    val email: String = "",
    val phone: String = ""
)

data class CasoDto(
    val id: String,
    val admin_officer_id: String = "",
    val assigned_agents: List<String> = emptyList(),
    val missing_person: DesaparecidoDto = DesaparecidoDto(),
    val external_contact: ExternalContactDto = ExternalContactDto(),
    val status: String = "",
    val total_reports: Int = 0,
    val created_at: String = ""
)

data class CasosPaginadosDto(
    val data: List<CasoDto>,
    val total: Long,
    val page: Int,
    val limit: Int,
    val hasMore: Boolean
)
