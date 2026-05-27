package com.trobat.ui.viewmodel

data class CaptureEvidenceUiState(
    val hasPhoto: Boolean = false,
    val isCapturing: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val errorMessage: String? = null
) {
    val hasRequiredPermissions: Boolean
        get() = hasCameraPermission && hasLocationPermission
}