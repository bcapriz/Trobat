package com.trobat.data.repository

sealed class AuthError(message: String) : Exception(message) {
    data object EmptyResponse : AuthError("Respuesta vacía del servidor")
    data object InvalidCredentials : AuthError("Credenciales inválidas")
    data object EmailAlreadyRegistered : AuthError("El email ya está registrado")
}
