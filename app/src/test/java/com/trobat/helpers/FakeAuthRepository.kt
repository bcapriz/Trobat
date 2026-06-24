package com.trobat.helpers

import com.trobat.data.repository.AuthRepository

class FakeAuthRepository(
    private val loginResult: Result<Unit> = Result.success(Unit),
    private val registerResult: Result<Unit> = Result.success(Unit),
    private val loggedIn: Boolean = false,
    private val userName: String? = "Test User",
    private val userId: String? = "user-123",
    private val nationalId: String? = "12345678",
    private val phone: String? = "1155551234",
    private val email: String? = "test@example.com"
) : AuthRepository {

    var logoutCalled = false

    override suspend fun login(email: String, password: String): Result<Unit> = loginResult

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        nationalId: String,
        phone: String
    ): Result<Unit> = registerResult

    override suspend fun logout() {
        logoutCalled = true
    }

    override fun isLoggedIn(): Boolean = loggedIn
    override fun getUserName(): String? = userName
    override fun getUserId(): String? = userId
    override fun getNationalId(): String? = nationalId
    override fun getPhone(): String? = phone
    override fun getEmail(): String? = email
}
