package com.trobat.data.remote.dto

data class LoginRequestDto(
    val email: String,
    val password: String,
    val fcm_token: String? = null
)

data class RegistroRequestDto(
    val name: String,
    val email: String,
    val password: String
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

data class MensajeResponseDto(
    val mensaje: String
)

data class CrearResponseDto(
    val id: String,
    val mensaje: String
)
