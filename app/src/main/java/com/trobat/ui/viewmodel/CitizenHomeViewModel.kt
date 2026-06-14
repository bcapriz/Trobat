package com.trobat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trobat.data.repository.CaseRepository
import com.trobat.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CitizenHomeViewModel : ViewModel() {

    private val caseRepository: CaseRepository = RepositoryProvider.caseRepository

    private val _uiState = MutableStateFlow(CitizenHomeUiState())
    val uiState: StateFlow<CitizenHomeUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CitizenHomeEffect>()
    val effect: SharedFlow<CitizenHomeEffect> = _effect.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _expandedCaseId = MutableStateFlow<String?>(null)

    init {
        observeData()
    }

    fun onEvent(event: CitizenHomeEvent) {
        when (event) {
            CitizenHomeEvent.OpenMapClicked -> navigateToMap()
            CitizenHomeEvent.CaptureEvidenceClicked -> navigateToCamera()
            CitizenHomeEvent.RefreshClicked -> {
                _searchQuery.value = ""
                _expandedCaseId.value = null
            }
            is CitizenHomeEvent.SearchQueryChanged -> _searchQuery.value = event.query
            is CitizenHomeEvent.CaseCardClicked -> {
                _expandedCaseId.value =
                    if (_expandedCaseId.value == event.caseId) null else event.caseId
            }
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                caseRepository.cases,
                _searchQuery,
                _expandedCaseId
            ) { cases, query, expandedId ->
                CitizenHomeUiState(
                    activeCases = cases,
                    searchQuery = query,
                    expandedCaseId = expandedId
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun navigateToMap() {
        viewModelScope.launch { _effect.emit(CitizenHomeEffect.NavigateToMap) }
    }

    private fun navigateToCamera() {
        viewModelScope.launch { _effect.emit(CitizenHomeEffect.NavigateToCamera) }
    }
}
