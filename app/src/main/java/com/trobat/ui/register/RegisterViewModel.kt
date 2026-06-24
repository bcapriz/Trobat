package com.trobat.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trobat.data.repository.AppContainer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val authRepository = AppContainer.authRepository

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<RegisterEffect>()
    val effect: SharedFlow<RegisterEffect> = _effect.asSharedFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.NameChanged -> _uiState.value = _uiState.value.copy(name = event.value, error = null)
            is RegisterEvent.EmailChanged -> _uiState.value = _uiState.value.copy(email = event.value, error = null)
            is RegisterEvent.PasswordChanged -> _uiState.value = _uiState.value.copy(password = event.value, error = null)
            is RegisterEvent.NationalIdChanged -> _uiState.value = _uiState.value.copy(nationalId = event.value, error = null)
            is RegisterEvent.PhoneChanged -> _uiState.value = _uiState.value.copy(phone = event.value, error = null)
            RegisterEvent.RegisterClicked -> register()
            RegisterEvent.LoginClicked -> viewModelScope.launch { _effect.emit(RegisterEffect.NavigateToLogin) }
        }
    }

    private fun register() {
        val state = _uiState.value
        if (state.name.isBlank() || state.email.isBlank() || state.password.isBlank() ||
            state.nationalId.isBlank() || state.phone.isBlank()
        ) {
            _uiState.value = state.copy(error = "Completá todos los campos")
            return
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(error = "La contraseña debe tener al menos 6 caracteres")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.register(
                name = state.name.trim(),
                email = state.email.trim(),
                password = state.password,
                nationalId = state.nationalId.trim(),
                phone = state.phone.trim()
            )
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _effect.emit(RegisterEffect.NavigateToLogin)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error al registrarse"
                )
            }
        }
    }
}
