package com.trobat.data.repository

import com.trobat.data.local.SessionManager
import com.trobat.data.remote.TrobatApi
import com.trobat.data.remote.dto.LoginRequestDto
import com.trobat.data.remote.dto.LogoutRequestDto
import com.trobat.data.remote.dto.RegistroRequestDto

class RemoteAuthRepository(
    private val api: TrobatApi,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = api.login(LoginRequestDto(email = email, password = password))
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(Exception("Respuesta vacía"))
                sessionManager.token = body.token
                sessionManager.userId = body.id
                sessionManager.userName = body.nombre
                Result.success(Unit)
            } else {
                Result.failure(Exception("Credenciales inválidas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<Unit> {
        return try {
            val response = api.registro(RegistroRequestDto(name = name, email = email, password = password))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("El email ya está registrado"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            api.logout(LogoutRequestDto())
        } catch (_: Exception) {
        }
        sessionManager.clear()
    }

    override fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    override fun getUserName(): String? = sessionManager.userName

    override fun getUserId(): String? = sessionManager.userId
}
