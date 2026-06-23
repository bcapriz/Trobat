package com.trobat.data.remote.dto

data class PersonalInfoDto(
    val national_id: String = "",
    val full_name: String = "",
    val phone: String = ""
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class RegistroRequestDto(
    val email: String,
    val password: String,
    val personal_info: PersonalInfoDto
)

data class LogoutRequestDto(
    val fcm_token: String = ""
)

data class TokenResponseDto(
    val token: String,
    val tipo: String,
    val id: String,
    val nombre: String
)

data class PersonalInfoResponseDto(
    val national_id: String = "",
    val full_name: String = "",
    val phone: String = ""
)

data class PerfilResponseDto(
    val id: String,
    val email: String,
    val personal_info: PersonalInfoResponseDto
)

data class MensajeResponseDto(
    val message: String
)

data class CrearResponseDto(
    val id: String,
    val message: String
)
