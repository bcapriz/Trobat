package com.trobat.ui.capture

import android.net.Uri

data class CaptureEvidenceUiState(
    val hasPhoto: Boolean = false,
    val isCapturing: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val capturedPhotoUri: Uri? = null,
    val capturedLatitude: Double? = null,
    val capturedLongitude: Double? = null,
    val errorMessage: String? = null
) {
    val hasRequiredPermissions: Boolean
        get() = hasCameraPermission && hasLocationPermission
}
