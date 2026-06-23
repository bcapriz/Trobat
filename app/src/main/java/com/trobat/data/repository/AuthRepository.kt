package com.trobat.data.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(name: String, email: String, password: String, nationalId: String, phone: String): Result<Unit>
    suspend fun logout()
    fun isLoggedIn(): Boolean
    fun getUserName(): String?
    fun getUserId(): String?
    fun getNationalId(): String?
    fun getPhone(): String?
    fun getEmail(): String?
    fun getNotificationsEnabled(): Boolean
    fun setNotificationsEnabled(enabled: Boolean)
    fun getDarkModeEnabled(): Boolean
    fun setDarkModeEnabled(enabled: Boolean)
}
