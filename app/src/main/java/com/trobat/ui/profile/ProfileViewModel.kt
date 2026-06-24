package com.trobat.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trobat.data.repository.AppContainer
import com.trobat.ui.theme.ThemeManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val authRepository = AppContainer.authRepository
    private val userPreferencesRepository = AppContainer.userPreferencesRepository

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            name = authRepository.getUserName() ?: "",
            email = authRepository.getEmail() ?: "",
            nationalId = authRepository.getNationalId() ?: "",
            phone = authRepository.getPhone() ?: "",
            notificationsEnabled = userPreferencesRepository.getNotificationsEnabled(),
            darkModeEnabled = userPreferencesRepository.getDarkModeEnabled()
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ProfileEffect>()
    val effect: SharedFlow<ProfileEffect> = _effect.asSharedFlow()

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.LogoutClicked -> logout()
            is ProfileEvent.NotificationsToggled -> {
                userPreferencesRepository.setNotificationsEnabled(event.enabled)
                _uiState.value = _uiState.value.copy(notificationsEnabled = event.enabled)
            }
            is ProfileEvent.DarkModeToggled -> {
                userPreferencesRepository.setDarkModeEnabled(event.enabled)
                ThemeManager.setDarkMode(event.enabled)
                _uiState.value = _uiState.value.copy(darkModeEnabled = event.enabled)
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _effect.emit(ProfileEffect.NavigateToLogin)
        }
    }
}
