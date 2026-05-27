package com.trobat.ui.viewmodel

import com.trobat.data.model.ActiveCase

data class ConfirmReportUiState(
    val activeCases: List<ActiveCase> = emptyList(),
    val selectedCaseId: String? = null,
    val requiredDescription: String = "",
    val optionalDetails: String = "",
    val showCaseError: Boolean = false,
    val showRequiredDescriptionError: Boolean = false,
    val isSending: Boolean = false
) {
    val selectedCase: ActiveCase?
        get() = activeCases.firstOrNull { it.id == selectedCaseId }

    val canSendReport: Boolean
        get() = selectedCaseId != null &&
                requiredDescription.isNotBlank() &&
                !isSending
}