package com.trobat.ui.viewmodel

sealed interface  CaptureEvidenceEffect {
    data object NavigateToConfirmReport : CaptureEvidenceEffect
}