package com.trobat.ui.viewmodel

import android.net.Uri
import com.trobat.domain.model.MissingPersonCase

data class ConfirmReportUiState(
    val activeCases: List<MissingPersonCase> = emptyList(),
    val selectedCaseId: String? = null,
    val requiredDescription: String = "",
    val optionalDetails: String = "",
    val showCaseError: Boolean = false,
    val showRequiredDescriptionError: Boolean = false,
    val isSending: Boolean = false,
    val photoUri: Uri? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isIdentified: Boolean = false
) {
    val selectedCase: MissingPersonCase?
        get() = activeCases.firstOrNull { it.id == selectedCaseId }

    val canSendReport: Boolean
        get() = selectedCaseId != null &&
                requiredDescription.isNotBlank() &&
                !isSending
}
