package com.trobat.ui.viewmodel

import com.trobat.domain.model.MissingPersonCase

data class HeatMapUiState(
    val cases: List<MissingPersonCase> = emptyList(),
    val totalCases: Int = 0,
    val mostActiveArea: String = "-",
    val isLoading: Boolean = true,
    val expandedCaseId: String? = null
)
