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
    private val _expandedCaseId = MutableStateFlow<String?>(null)
    private val _radiusKm = MutableStateFlow(10f)
    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)

    init {
        fetchUserLocation()
        observeData()
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

    fun onEvent(event: CitizenHomeEvent) {
        when (event) {
            CitizenHomeEvent.OpenMapClicked -> navigateToMap()
            CitizenHomeEvent.CaptureEvidenceClicked -> navigateToCamera()
            CitizenHomeEvent.RefreshClicked -> {
                _searchQuery.value = ""
                _expandedCaseId.value = null
                fetchUserLocation()
            }
            is CitizenHomeEvent.SearchQueryChanged -> _searchQuery.value = event.query
            is CitizenHomeEvent.CaseCardClicked -> {
                _expandedCaseId.value =
                    if (_expandedCaseId.value == event.caseId) null else event.caseId
            }
            is CitizenHomeEvent.RadiusChanged -> _radiusKm.value = event.km
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                caseRepository.cases,
                _searchQuery,
                _expandedCaseId,
                _radiusKm,
                _userLocation
            ) { cases, query, expandedId, radius, location ->
                CitizenHomeUiState(
                    activeCases = cases,
                    searchQuery = query,
                    expandedCaseId = expandedId,
                    userLat = location?.first,
                    userLng = location?.second,
                    radiusKm = radius
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
