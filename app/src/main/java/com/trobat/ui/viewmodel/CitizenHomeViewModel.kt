package com.trobat.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trobat.data.repository.CaseRepository
import com.trobat.data.repository.RepositoryProvider
import com.trobat.utils.fetchCurrentLocation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CitizenHomeViewModel(app: Application) : AndroidViewModel(app) {

    private val caseRepository: CaseRepository = RepositoryProvider.caseRepository

    private val _uiState = MutableStateFlow(CitizenHomeUiState())
    val uiState: StateFlow<CitizenHomeUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CitizenHomeEffect>()
    val effect: SharedFlow<CitizenHomeEffect> = _effect.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCase = MutableStateFlow<com.trobat.data.model.MissingPersonCase?>(null)
    private val _radiusKm = MutableStateFlow(50f)
    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    private val _isLoading = MutableStateFlow(true)

    init {
        fetchUserLocation()
        observeData()
    }

    private fun fetchUserLocation() {
        fetchCurrentLocation { location ->
            _userLocation.value = location
            if (location != null) RepositoryProvider.lastLocationPrefs.save(location.first, location.second)
            viewModelScope.launch {
                _isLoading.value = true
                if (location != null) {
                    caseRepository.refreshCercanosConFallback(location.first, location.second, _radiusKm.value.toDouble())
                } else {
                    caseRepository.refresh()
                }
                _isLoading.value = false
            }
        }
    }

    fun onEvent(event: CitizenHomeEvent) {
        when (event) {
            CitizenHomeEvent.OpenMapClicked -> navigateToMap()
            CitizenHomeEvent.CaptureEvidenceClicked -> navigateToCamera()
            CitizenHomeEvent.RefreshClicked -> {
                _searchQuery.value = ""
                _selectedCase.value = null
                fetchUserLocation()
            }
            CitizenHomeEvent.DismissCaseModal -> _selectedCase.value = null
            is CitizenHomeEvent.SearchQueryChanged -> _searchQuery.value = event.query
            is CitizenHomeEvent.CaseCardClicked -> _selectedCase.value = event.case
            is CitizenHomeEvent.RadiusChanged -> _radiusKm.value = event.km
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                caseRepository.cases,
                _searchQuery,
                _selectedCase,
                _radiusKm,
                _userLocation
            ) { cases, query, selectedCase, radius, location ->
                CitizenHomeUiState(
                    activeCases = cases,
                    searchQuery = query,
                    selectedCase = selectedCase,
                    userLat = location?.first,
                    userLng = location?.second,
                    radiusKm = radius
                )
            }.combine(_isLoading) { state, loading ->
                state.copy(isLoading = loading)
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
