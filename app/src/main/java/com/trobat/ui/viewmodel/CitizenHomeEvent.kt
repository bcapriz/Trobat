package com.trobat.ui.viewmodel

sealed interface CitizenHomeEvent {
    data object OpenMapClicked : CitizenHomeEvent
    data object CaptureEvidenceClicked : CitizenHomeEvent
    data object RefreshClicked : CitizenHomeEvent
}