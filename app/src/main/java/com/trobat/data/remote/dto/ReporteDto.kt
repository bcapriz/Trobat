package com.trobat.data.remote.dto

data class SecurityMetadataDto(
    val anonymous: Boolean = true
)

data class ContactInfoDto(
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null
)

data class CrearReporteRequestDto(
    val case_id: String,
    val location: UbicacionDto,
    val description: String,
    val police_priority: Boolean = false,
    val security_metadata: SecurityMetadataDto = SecurityMetadataDto(),
    val contact_info: ContactInfoDto = ContactInfoDto()
)

data class ReporteDto(
    val id: String,
    val case_id: String,
    val location: UbicacionDto = UbicacionDto(),
    val location_label: String? = null,
    val timestamp: String = "",
    val description: String = "",
    val photo_url: String? = null,
    val security_metadata: SecurityMetadataDto = SecurityMetadataDto(),
    val contact_info: ContactInfoDto = ContactInfoDto(),
    val validated: Boolean = false,
    val priority: String? = null
)

data class ReportesPaginadosDto(
    val data: List<ReporteDto>,
    val total: Long,
    val page: Int,
    val limit: Int,
    val hasMore: Boolean
)
