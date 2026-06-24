package com.trobat.ui.profile

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val nationalId: String = "",
    val phone: String = "",
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false
)
