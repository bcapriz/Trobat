package com.trobat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trobat.data.repository.CaseRepository
import com.trobat.data.repository.CitizenReportRepository
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

    private val reportRepository: CitizenReportRepository = RepositoryProvider.citizenReportRepository
    private val caseRepository: CaseRepository = RepositoryProvider.caseRepository

    private val _uiState = MutableStateFlow(CitizenHomeUiState(isLoading = true))
    val uiState: StateFlow<CitizenHomeUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CitizenHomeEffect>()
    val effect: SharedFlow<CitizenHomeEffect> = _effect.asSharedFlow()

    init {
        observeData()
    }

    fun onEvent(event: CitizenHomeEvent) {
        when (event) {
            CitizenHomeEvent.OpenMapClicked -> navigateToMap()
            CitizenHomeEvent.CaptureEvidenceClicked -> navigateToCamera()
            CitizenHomeEvent.RefreshClicked -> Unit
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                reportRepository.reports,
                caseRepository.cases
            ) { reports, cases ->
                CitizenHomeUiState(
                    activeCases = cases,
                    nearbyReports = reports,
                    unreadNotifications = reports.count { it.status.name == "NEW" },
                    isLoading = false
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
