package com.trobat.ui.report

sealed interface ConfirmReportEffect {
    data object NavigateToHeatMap : ConfirmReportEffect
    data object NavigateBackToCamera : ConfirmReportEffect
    data object ReportSavedLocally : ConfirmReportEffect
    data object NavigateToCases : ConfirmReportEffect
}
