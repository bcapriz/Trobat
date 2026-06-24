package com.trobat.data.repository

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.trobat.TrobatApplication
import com.trobat.data.local.LastLocationPrefs
import com.trobat.data.local.SessionManager
import com.trobat.data.local.TrobatDatabase
import com.trobat.data.remote.TrobatApi
import com.trobat.data.remote.dto.LoginRequestDto
import com.trobat.data.remote.dto.LogoutRequestDto
import com.trobat.data.remote.dto.PersonalInfoDto
import com.trobat.data.remote.dto.PerfilResponseDto
import com.trobat.data.remote.dto.RegistroRequestDto

class RemoteAuthRepository(
    private val api: TrobatApi,
    private val sessionManager: SessionManager,
    private val db: TrobatDatabase,
    private val lastLocationPrefs: LastLocationPrefs,
    private val context: Context
) : AuthRepository, UserPreferencesRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = api.login(LoginRequestDto(email = email, password = password))
            if (response.isSuccessful) {
                val body = response.body() ?: return Result.failure(AuthError.EmptyResponse)
                sessionManager.token = body.token
                sessionManager.userId = body.id
                sessionManager.userName = body.nombre
                fetchAndSavePerfil()
                FirebaseMessaging.getInstance().subscribeToTopic(TrobatApplication.ALERTS_TOPIC)
                Result.success(Unit)
            } else {
                Result.failure(AuthError.InvalidCredentials)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchAndSavePerfil() {
        try {
            val perfil = api.getPerfil()
            if (perfil.isSuccessful) {
                val body = perfil.body() ?: return
                sessionManager.email = body.email
                sessionManager.nationalId = body.personal_info.national_id.ifBlank { null }
                sessionManager.phone = body.personal_info.phone.ifBlank { null }
                sessionManager.userName = body.personal_info.full_name.ifBlank { sessionManager.userName }
            }
        } catch (e: Exception) {
            android.util.Log.w("RemoteAuthRepository", "fetchAndSavePerfil failed", e)
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        nationalId: String,
        phone: String
    ): Result<Unit> {
        return try {
            val response = api.registro(RegistroRequestDto(
                email = email,
                password = password,
                personal_info = PersonalInfoDto(
                    full_name = name,
                    national_id = nationalId,
                    phone = phone
                )
            ))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(AuthError.EmailAlreadyRegistered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            api.logout(LogoutRequestDto())
        } catch (_: Exception) {
        }
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TrobatApplication.ALERTS_TOPIC)
        sessionManager.clear()
        lastLocationPrefs.clear()
        db.caseDao().deleteAll()
        db.notificationDao().deleteAll()
        db.pendingReportDao().deleteAll()
        NotificationManagerCompat.from(context).cancelAll()
    }

    override fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    override fun getUserName(): String? = sessionManager.userName

    override fun getUserId(): String? = sessionManager.userId

    override fun getNationalId(): String? = sessionManager.nationalId

    override fun getPhone(): String? = sessionManager.phone

    override fun getEmail(): String? = sessionManager.email

    override fun getNotificationsEnabled(): Boolean = sessionManager.notificationsEnabled

    override fun setNotificationsEnabled(enabled: Boolean) {
        sessionManager.notificationsEnabled = enabled
    }

    override fun getDarkModeEnabled(): Boolean = sessionManager.darkModeEnabled

    override fun setDarkModeEnabled(enabled: Boolean) {
        sessionManager.darkModeEnabled = enabled
    }
}
