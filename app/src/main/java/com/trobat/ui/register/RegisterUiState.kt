package com.trobat.ui.register

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val nationalId: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
