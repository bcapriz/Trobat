package com.trobat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trobat.data.repository.CitizenReportRepository
import com.trobat.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CitizenHomeViewModel : ViewModel() {

    private val repository: CitizenReportRepository =
        RepositoryProvider.citizenReportRepository

    private val _uiState = MutableStateFlow(
        CitizenHomeUiState(isLoading = true)
    )
    val uiState: StateFlow<CitizenHomeUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CitizenHomeEffect>()
    val effect: SharedFlow<CitizenHomeEffect> = _effect.asSharedFlow()

    init {
        observeReports()
    }

    fun onEvent(event: CitizenHomeEvent) {
        when (event) {
            CitizenHomeEvent.OpenMapClicked -> {
                navigateToMap()
            }

            CitizenHomeEvent.CaptureEvidenceClicked -> {
                navigateToCamera()
            }

            CitizenHomeEvent.RefreshClicked -> {
                loadHomeData()
            }
        }
    }

    private fun observeReports() {
        viewModelScope.launch {
            repository.reports.collect { reports ->
                _uiState.value = _uiState.value.copy(
                    nearbyReports = reports,
                    unreadNotifications = repository.getUnreadNotificationsCount(),
                    isLoading = false,
                    errorMessage = null
                )
            }
        }
    }

    fun loadHomeData() {
        val reports = repository.getNearbyReports()
        val unreadNotifications = repository.getUnreadNotificationsCount()

        _uiState.value = _uiState.value.copy(
            nearbyReports = reports,
            unreadNotifications = unreadNotifications,
            isLoading = false,
            errorMessage = null
        )
    }

    private fun navigateToMap() {
        viewModelScope.launch {
            _effect.emit(CitizenHomeEffect.NavigateToMap)
        }
    }

    private fun navigateToCamera() {
        viewModelScope.launch {
            _effect.emit(CitizenHomeEffect.NavigateToCamera)
        }
    }
}
