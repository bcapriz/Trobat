package com.trobat.ui.viewmodel

sealed interface CitizenHomeEvent {
    data object OpenMapClicked : CitizenHomeEvent
    data object CaptureEvidenceClicked : CitizenHomeEvent
    data object RefreshClicked : CitizenHomeEvent
    data class SearchQueryChanged(val query: String) : CitizenHomeEvent
    data class CaseCardClicked(val caseId: String) : CitizenHomeEvent
    data class RadiusChanged(val km: Float) : CitizenHomeEvent
}
