package com.trobat.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.trobat.data.repository.CaseRepository
import com.trobat.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HeatMapViewModel(app: Application) : AndroidViewModel(app) {

    private val caseRepository: CaseRepository = RepositoryProvider.caseRepository

    private val _uiState = MutableStateFlow(HeatMapUiState())
    val uiState: StateFlow<HeatMapUiState> = _uiState.asStateFlow()

    private val _radiusKm = MutableStateFlow(10f)
    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)

    init {
        fetchUserLocation()
        observeCases()
    }

    @SuppressLint("MissingPermission")
    private fun fetchUserLocation() {
        val client = LocationServices.getFusedLocationProviderClient(getApplication<Application>())
        val tokenSource = CancellationTokenSource()
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    _userLocation.value = Pair(location.latitude, location.longitude)
                }
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
