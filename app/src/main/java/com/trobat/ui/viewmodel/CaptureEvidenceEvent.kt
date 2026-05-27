package com.trobat.ui.viewmodel

sealed interface CaptureEvidenceEvent {
    data class PermissionsChanged(
        val hasCameraPermission: Boolean,
        val hasLocationPermission: Boolean
    ) : CaptureEvidenceEvent

    data object TakePhotoClicked : CaptureEvidenceEvent
    data object UseEvidenceClicked : CaptureEvidenceEvent
    data object RetakePhotoClicked : CaptureEvidenceEvent
}