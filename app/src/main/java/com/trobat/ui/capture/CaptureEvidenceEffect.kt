package com.trobat.ui.capture

sealed interface  CaptureEvidenceEffect {
    data object NavigateToConfirmReport : CaptureEvidenceEffect
}