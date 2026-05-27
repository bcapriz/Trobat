package com.trobat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CaptureEvidenceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureEvidenceUiState())
    val uiState: StateFlow<CaptureEvidenceUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CaptureEvidenceEffect>()
    val effect: SharedFlow<CaptureEvidenceEffect> = _effect.asSharedFlow()

    fun onEvent(event: CaptureEvidenceEvent) {
        when (event) {
            is CaptureEvidenceEvent.PermissionsChanged -> {
                onPermissionsChanged(
                    hasCameraPermission = event.hasCameraPermission,
                    hasLocationPermission = event.hasLocationPermission
                )
            }

            CaptureEvidenceEvent.TakePhotoClicked -> {
                takePhoto()
            }

            CaptureEvidenceEvent.UseEvidenceClicked -> {
                useEvidence()
            }

            CaptureEvidenceEvent.RetakePhotoClicked -> {
                retakePhoto()
            }
        }
    }

    private fun onPermissionsChanged(
        hasCameraPermission: Boolean,
        hasLocationPermission: Boolean
    ) {
        _uiState.value = _uiState.value.copy(
            hasCameraPermission = hasCameraPermission,
            hasLocationPermission = hasLocationPermission,
            errorMessage = null
        )
    }

    private fun takePhoto() {
        val currentState = _uiState.value

        if (!currentState.hasRequiredPermissions) {
            _uiState.value = currentState.copy(
                errorMessage = "Necesitamos permisos de cámara y ubicación para continuar."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isCapturing = true,
                errorMessage = null
            )

            delay(700)

            _uiState.value = _uiState.value.copy(
                hasPhoto = true,
                isCapturing = false
            )
        }
    }

    private fun useEvidence() {
        viewModelScope.launch {
            if (_uiState.value.hasPhoto) {
                _effect.emit(CaptureEvidenceEffect.NavigateToConfirmReport)
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Primero tenés que tomar una foto."
                )
            }
        }
    }

    private fun retakePhoto() {
        _uiState.value = _uiState.value.copy(
            hasPhoto = false,
            isCapturing = false,
            errorMessage = null
        )
    }
}