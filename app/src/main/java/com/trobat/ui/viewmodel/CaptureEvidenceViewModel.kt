package com.trobat.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trobat.data.model.CapturedEvidenceHolder
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
            is CaptureEvidenceEvent.PermissionsChanged -> onPermissionsChanged(
                hasCameraPermission = event.hasCameraPermission,
                hasLocationPermission = event.hasLocationPermission
            )
            CaptureEvidenceEvent.TakePhotoClicked -> startCapture()
            is CaptureEvidenceEvent.PhotoCaptured -> onPhotoCaptured(event.uri, event.latitude, event.longitude)
            is CaptureEvidenceEvent.CaptureError -> _uiState.value = _uiState.value.copy(
                isCapturing = false,
                errorMessage = event.message ?: "Error al capturar foto"
            )
            CaptureEvidenceEvent.UseEvidenceClicked -> useEvidence()
            CaptureEvidenceEvent.RetakePhotoClicked -> retakePhoto()
        }
    }

    private fun onPermissionsChanged(hasCameraPermission: Boolean, hasLocationPermission: Boolean) {
        _uiState.value = _uiState.value.copy(
            hasCameraPermission = hasCameraPermission,
            hasLocationPermission = hasLocationPermission,
            errorMessage = null
        )
    }

    private fun startCapture() {
        val state = _uiState.value
        if (!state.hasRequiredPermissions) {
            _uiState.value = state.copy(errorMessage = "Necesitamos permisos de cámara y ubicación para continuar.")
            return
        }
        _uiState.value = state.copy(isCapturing = true, errorMessage = null)
    }

    private fun onPhotoCaptured(uri: Uri, latitude: Double, longitude: Double) {
        CapturedEvidenceHolder.photoUri = uri
        CapturedEvidenceHolder.latitude = latitude
        CapturedEvidenceHolder.longitude = longitude

        _uiState.value = _uiState.value.copy(
            hasPhoto = true,
            isCapturing = false,
            capturedPhotoUri = uri,
            capturedLatitude = latitude,
            capturedLongitude = longitude
        )
    }

    private fun useEvidence() {
        viewModelScope.launch {
            if (_uiState.value.hasPhoto) {
                _effect.emit(CaptureEvidenceEffect.NavigateToConfirmReport)
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Primero tenés que tomar una foto.")
            }
        }
    }

    private fun retakePhoto() {
        CapturedEvidenceHolder.clear()
        _uiState.value = _uiState.value.copy(
            hasPhoto = false,
            isCapturing = false,
            capturedPhotoUri = null,
            capturedLatitude = null,
            capturedLongitude = null,
            errorMessage = null
        )
    }
}
