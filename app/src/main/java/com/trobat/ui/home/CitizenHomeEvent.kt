package com.trobat.ui.home

import com.trobat.data.model.MissingPersonCase

sealed interface CitizenHomeEvent {
    data object OpenMapClicked : CitizenHomeEvent
    data object CaptureEvidenceClicked : CitizenHomeEvent
    data object RefreshClicked : CitizenHomeEvent
    data object DismissCaseModal : CitizenHomeEvent
    data class SearchQueryChanged(val query: String) : CitizenHomeEvent
    data class CaseCardClicked(val case: MissingPersonCase) : CitizenHomeEvent
    data class RadiusChanged(val km: Float) : CitizenHomeEvent
    data object ResumeDraftClicked : CitizenHomeEvent
    data object ScreenResumed : CitizenHomeEvent
    data class CaseSelectedForReport(val caseId: String) : CitizenHomeEvent
}
