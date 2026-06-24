package com.trobat.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trobat.data.repository.AppContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val authRepository = AppContainer.authRepository
    private val onboardingPrefs = AppContainer.onboardingPrefs

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SplashEffect>()
    val effect: SharedFlow<SplashEffect> = _effect.asSharedFlow()

    private var started = false

    fun startSplash() {
        if (started) return
        started = true
        viewModelScope.launch {
            delay(1800)
            _uiState.update { it.copy(showLoading = true) }
            delay(1800)
            val destination = when {
                authRepository.isLoggedIn() -> SplashEffect.NavigateToMain
                !onboardingPrefs.hasSeenOnboarding -> SplashEffect.NavigateToOnboarding
                else -> SplashEffect.NavigateToLogin
            }
            _effect.emit(destination)
        }
    }
}
