package com.trobat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trobat.data.repository.CaseRepository
import com.trobat.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HeatMapViewModel : ViewModel() {

    private val caseRepository: CaseRepository = RepositoryProvider.caseRepository

    private val _uiState = MutableStateFlow(HeatMapUiState())
    val uiState: StateFlow<HeatMapUiState> = _uiState.asStateFlow()

    init {
        observeCases()
    }

    fun onCaseClicked(caseId: String) {
        _uiState.update { state ->
            state.copy(expandedCaseId = if (state.expandedCaseId == caseId) null else caseId)
        }
    }

    private fun observeCases() {
        viewModelScope.launch {
            caseRepository.cases.collect { cases ->
                val areaFrequency = cases.groupingBy { it.area }.eachCount()
                val mostActive = areaFrequency.maxByOrNull { it.value }?.key ?: "-"

                _uiState.update { state ->
                    state.copy(
                        cases = cases,
                        totalCases = cases.size,
                        mostActiveArea = mostActive,
                        isLoading = false
                    )
                }
            }
        }
    }
}
