package com.trobat.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trobat.data.repository.CaseRepository
import com.trobat.data.repository.RepositoryProvider
import com.trobat.utils.ConcentrationUtil
import com.trobat.utils.fetchCurrentLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HeatMapViewModel(app: Application) : AndroidViewModel(app) {

    private val caseRepository: CaseRepository = RepositoryProvider.caseRepository

    private val _uiState = MutableStateFlow(HeatMapUiState())
    val uiState: StateFlow<HeatMapUiState> = _uiState.asStateFlow()

    private val _radiusKm = MutableStateFlow(50f)
    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    private val _isLoading = MutableStateFlow(true)

    init {
        fetchUserLocation()
        observeCases()
    }

    private fun fetchUserLocation() {
        fetchCurrentLocation { location ->
            _userLocation.value = location
            if (location != null) RepositoryProvider.lastLocationPrefs.save(location.first, location.second)
            viewModelScope.launch {
                if (caseRepository.cases.value.isNotEmpty()) {
                    _isLoading.value = false
                    return@launch
                }
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

    fun onCaseClicked(case: com.trobat.data.model.MissingPersonCase) {
        _uiState.update { state -> state.copy(selectedCase = case) }
    }

    fun onDismissCaseModal() {
        _uiState.update { state -> state.copy(selectedCase = null) }
    }

    fun onRadiusChanged(km: Float) {
        _radiusKm.value = km
    }

    private fun observeCases() {
        viewModelScope.launch {
            combine(
                caseRepository.cases,
                _radiusKm,
                _userLocation
            ) { cases, radius, location ->
                val concentration = ConcentrationUtil.mostConcentrated(cases)
                HeatMapUiState(
                    cases = cases,
                    totalCases = cases.size,
                    mostActiveArea = concentration?.label ?: "-",
                    mostActiveCount = concentration?.count ?: 0,
                    selectedCase = _uiState.value.selectedCase,
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
}
