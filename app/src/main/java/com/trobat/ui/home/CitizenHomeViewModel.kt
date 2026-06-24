package com.trobat.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trobat.data.repository.CaseRepository
import com.trobat.data.repository.AppContainer
import com.trobat.ui.utils.fetchCurrentLocation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class CitizenHomeViewModel(app: Application) : AndroidViewModel(app) {

    private val caseRepository: CaseRepository = AppContainer.caseRepository
    private val draftPrefs = AppContainer.reportDraftPrefs

    private val _uiState = MutableStateFlow(CitizenHomeUiState())
    val uiState: StateFlow<CitizenHomeUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CitizenHomeEffect>()
    val effect: SharedFlow<CitizenHomeEffect> = _effect.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCase = MutableStateFlow<com.trobat.data.model.MissingPersonCase?>(null)
    private val _radiusKm = MutableStateFlow(50f)
    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _searchResults = MutableStateFlow<List<com.trobat.data.model.MissingPersonCase>?>(null)
    private val _isSearching = MutableStateFlow(false)
    private val _hasPendingDraft = MutableStateFlow(false)

    init {
        fetchUserLocation()
        observeData()
        observeSearch()
        checkPendingDraft()
    }

    private fun checkPendingDraft() {
        _hasPendingDraft.value = !draftPrefs.isEmpty()
    }

    private fun fetchUserLocation() {
        fetchCurrentLocation { location ->
            _userLocation.value = location
            if (location != null) AppContainer.lastLocationPrefs.save(location.first, location.second)
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
            CitizenHomeEvent.CaptureEvidenceClicked -> {
                if (!draftPrefs.isEmpty()) navigateToConfirmReport() else navigateToCamera()
            }
            CitizenHomeEvent.RefreshClicked -> {
                _searchQuery.value = ""
                _selectedCase.value = null
                fetchUserLocation()
            }
            CitizenHomeEvent.DismissCaseModal -> _selectedCase.value = null
            is CitizenHomeEvent.SearchQueryChanged -> {
                _searchQuery.value = event.query
                if (event.query.isBlank()) {
                    _searchResults.value = null
                    _isSearching.value = false
                }
            }
            is CitizenHomeEvent.CaseCardClicked -> {
                _selectedCase.value = event.case
                val isFromSearch = _searchResults.value?.any { it.id == event.case.id } == true
                if (isFromSearch) {
                    viewModelScope.launch { caseRepository.cacheCase(event.case) }
                }
            }
            is CitizenHomeEvent.RadiusChanged -> _radiusKm.value = event.km
            CitizenHomeEvent.ResumeDraftClicked -> navigateToConfirmReport()
            CitizenHomeEvent.ScreenResumed -> checkPendingDraft()
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
            }.combine(_searchResults) { state, searchResults ->
                state.copy(searchResults = searchResults)
            }.combine(_isSearching) { state, isSearching ->
                state.copy(isSearching = isSearching)
            }.combine(_hasPendingDraft) { state, hasPendingDraft ->
                state.copy(hasPendingDraft = hasPendingDraft)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collect { query ->
                    if (query.length >= 2) {
                        _isSearching.value = true
                        try {
                            _searchResults.value = caseRepository.searchByName(query)
                        } catch (_: Exception) {
                            _searchResults.value = emptyList()
                        } finally {
                            _isSearching.value = false
                        }
                    }
                }
        }
    }

    private fun navigateToMap() {
        viewModelScope.launch { _effect.emit(CitizenHomeEffect.NavigateToMap) }
    }

    private fun navigateToCamera() {
        viewModelScope.launch { _effect.emit(CitizenHomeEffect.NavigateToCamera) }
    }

    private fun navigateToConfirmReport() {
        viewModelScope.launch { _effect.emit(CitizenHomeEffect.NavigateToConfirmReport) }
    }
}
