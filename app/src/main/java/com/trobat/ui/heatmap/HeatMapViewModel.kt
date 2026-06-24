package com.trobat.ui.heatmap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trobat.data.model.MissingPersonCase
import com.trobat.data.repository.AppContainer
import com.trobat.data.repository.CaseRepository
import com.trobat.ui.capture.CapturedEvidenceHolder
import com.trobat.ui.utils.fetchCurrentLocation
import com.trobat.utils.ConcentrationUtil
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HeatMapViewModel(app: Application) : AndroidViewModel(app) {

    private val caseRepository: CaseRepository = AppContainer.caseRepository

    private val _uiState = MutableStateFlow(HeatMapUiState())
    val uiState: StateFlow<HeatMapUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<HeatMapEffect>()
    val effect: SharedFlow<HeatMapEffect> = _effect.asSharedFlow()

    private val _radiusKm = MutableStateFlow(50f)
    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)

    init {
        fetchUserLocation()
        observeCases()
    }

    private fun fetchUserLocation() {
        fetchCurrentLocation { location ->
            _userLocation.value = location
            if (location != null) AppContainer.lastLocationPrefs.save(location.first, location.second)
            viewModelScope.launch {
                if (caseRepository.cases.value.isNotEmpty()) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }
                _uiState.update { it.copy(isLoading = true) }
                if (location != null) {
                    caseRepository.refreshCercanosConFallback(location.first, location.second, _radiusKm.value.toDouble())
                } else {
                    caseRepository.refresh()
                }
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onCaseClicked(case: MissingPersonCase) {
        _uiState.update { it.copy(selectedCase = case) }
    }

    fun onDismissCaseModal() {
        _uiState.update { it.copy(selectedCase = null) }
    }

    fun onRadiusChanged(km: Float) {
        _radiusKm.value = km
    }

    fun onCargarReporte(case: MissingPersonCase) {
        CapturedEvidenceHolder.preselectedCaseId = case.id
        viewModelScope.launch { _effect.emit(HeatMapEffect.NavigateToCamera) }
    }

    private fun observeCases() {
        viewModelScope.launch {
            combine(caseRepository.cases, _radiusKm, _userLocation) { cases, radius, location ->
                val concentration = ConcentrationUtil.mostConcentrated(cases)
                HeatMapUiState(
                    cases = cases,
                    totalCases = cases.size,
                    mostActiveArea = concentration?.label ?: "-",
                    mostActiveCount = concentration?.count ?: 0,
                    userLat = location?.first,
                    userLng = location?.second,
                    radiusKm = radius
                )
            }.collect { newState ->
                _uiState.update { current ->
                    newState.copy(
                        isLoading = current.isLoading,
                        selectedCase = current.selectedCase
                    )
                }
            }
        }
    }
}
