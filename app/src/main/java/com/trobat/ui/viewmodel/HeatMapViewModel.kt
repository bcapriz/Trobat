package com.trobat.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trobat.data.repository.CaseRepository
import com.trobat.data.repository.RepositoryProvider
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

    init {
        fetchUserLocation()
        observeCases()
    }

    private fun fetchUserLocation() {
        fetchCurrentLocation { location ->
            _userLocation.value = location
            if (location != null) {
                viewModelScope.launch {
                    caseRepository.refreshCercanos(location.first, location.second, _radiusKm.value.toDouble())
                }
            }
        }
    }

    fun onCaseClicked(caseId: String) {
        _uiState.update { state ->
            state.copy(expandedCaseId = if (state.expandedCaseId == caseId) null else caseId)
        }
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
                val areaFrequency = cases.groupingBy { it.area }.eachCount()
                val mostActive = areaFrequency.maxByOrNull { it.value }?.key ?: "-"
                HeatMapUiState(
                    cases = cases,
                    totalCases = cases.size,
                    mostActiveArea = mostActive,
                    isLoading = false,
                    expandedCaseId = _uiState.value.expandedCaseId,
                    userLat = location?.first,
                    userLng = location?.second,
                    radiusKm = radius
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
