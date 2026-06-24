package com.trobat.ui.report

import android.net.Uri
import com.trobat.data.model.MissingPersonCase

data class ConfirmReportUiState(
    val activeCases: List<MissingPersonCase> = emptyList(),
    val selectedCaseId: String? = null,
    val selectedCase: MissingPersonCase? = null,
    val selectedCaseName: String? = null,
    val caseSearchQuery: String = "",
    val caseSearchResults: List<MissingPersonCase>? = null,
    val isCaseSearching: Boolean = false,
    val requiredDescription: String = "",
    val optionalDetails: String = "",
    val showCaseError: Boolean = false,
    val showRequiredDescriptionError: Boolean = false,
    val isSending: Boolean = false,
    val photoUri: Uri? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationLabel: String = "Obteniendo dirección...",
    val isIdentified: Boolean = false
) {
    val selectedCaseLabel: String?
        get() = selectedCase?.let { "${it.fullName}, ${it.age} años" } ?: selectedCaseName

    val displayedCases: List<MissingPersonCase>
        get() = caseSearchResults ?: activeCases

    val canSendReport: Boolean
        get() = selectedCaseId != null &&
                requiredDescription.isNotBlank() &&
                !isSending
}
