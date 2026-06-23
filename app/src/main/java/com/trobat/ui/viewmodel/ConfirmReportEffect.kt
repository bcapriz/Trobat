package com.trobat.ui.viewmodel

sealed interface ConfirmReportEffect {
    data object NavigateToHeatMap : ConfirmReportEffect
    data object NavigateBackToCamera : ConfirmReportEffect
    data object ReportSavedLocally : ConfirmReportEffect
}
