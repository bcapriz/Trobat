package com.trobat.ui.viewmodel

import android.net.Uri

sealed interface CaptureEvidenceEvent {
    data class PermissionsChanged(
        val hasCameraPermission: Boolean,
        val hasLocationPermission: Boolean
    ) : CaptureEvidenceEvent

    data object TakePhotoClicked : CaptureEvidenceEvent
    data class PhotoCaptured(
        val uri: Uri,
        val latitude: Double,
        val longitude: Double
    ) : CaptureEvidenceEvent
    data class CaptureError(val message: String?) : CaptureEvidenceEvent
    data object UseEvidenceClicked : CaptureEvidenceEvent
    data object RetakePhotoClicked : CaptureEvidenceEvent
}
