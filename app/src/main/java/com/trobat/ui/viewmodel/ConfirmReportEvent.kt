package com.trobat.ui.viewmodel

sealed interface ConfirmReportEvent {
    data class CaseSelected(val caseId: String) : ConfirmReportEvent
    data class RequiredDescriptionChanged(val value: String) : ConfirmReportEvent
    data class OptionalDetailsChanged(val value: String) : ConfirmReportEvent
    data object SendReportClicked : ConfirmReportEvent
    data object RetakePhotoClicked : ConfirmReportEvent
    data class IdentificationToggled(val isIdentified: Boolean) : ConfirmReportEvent
    data object CancelClicked : ConfirmReportEvent
}