package com.trobat.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trobat.data.repository.AppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val authRepository = AppContainer.authRepository
    private val termsPrefs = AppContainer.termsPrefs

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect: SharedFlow<LoginEffect> = _effect.asSharedFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> _uiState.value = _uiState.value.copy(email = event.value, error = null)
            is LoginEvent.PasswordChanged -> _uiState.value = _uiState.value.copy(password = event.value, error = null)
            LoginEvent.LoginClicked -> login()
            LoginEvent.RegisterClicked -> viewModelScope.launch { _effect.emit(LoginEffect.NavigateToRegister) }
            LoginEvent.TermsAccepted -> acceptTerms()
            LoginEvent.TermsRejected -> rejectTerms()
        }
    }

    private fun login() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Completá todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.login(state.email.trim(), state.password)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                if (termsPrefs.hasAcceptedTerms) {
                    _effect.emit(LoginEffect.NavigateToMain)
                } else {
                    _uiState.value = _uiState.value.copy(showTermsDialog = true)
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error al iniciar sesión"
                )
            }
        }
    }

    private fun acceptTerms() {
        termsPrefs.hasAcceptedTerms = true
        _uiState.value = _uiState.value.copy(showTermsDialog = false)
        viewModelScope.launch { _effect.emit(LoginEffect.NavigateToMain) }
    }

    private fun rejectTerms() {
        viewModelScope.launch(Dispatchers.IO) { authRepository.logout() }
        _uiState.value = _uiState.value.copy(showTermsDialog = false)
    }
}
