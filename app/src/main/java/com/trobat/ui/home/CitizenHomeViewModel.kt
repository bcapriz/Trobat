package com.trobat.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.CancellationTokenSource
import com.trobat.data.repository.CaseRepository
import com.trobat.data.repository.AppContainer
import com.trobat.ui.capture.CapturedEvidenceHolder
import com.trobat.ui.utils.fetchCurrentLocation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CitizenHomeViewModel(app: Application) : AndroidViewModel(app) {

    private val caseRepository: CaseRepository = AppContainer.caseRepository
    private val draftPrefs = AppContainer.reportDraftPrefs

    private val _uiState = MutableStateFlow(CitizenHomeUiState())
    val uiState: StateFlow<CitizenHomeUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CitizenHomeEffect>()
    val effect: SharedFlow<CitizenHomeEffect> = _effect.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    private var locationTokenSource: CancellationTokenSource? = null

    init {
        observeCases()
        observeSearch()
        fetchUserLocation()
        checkPendingDraft()
    }

    override fun onCleared() {
        super.onCleared()
        locationTokenSource?.cancel()
    }

    private fun checkPendingDraft() {
        _uiState.update { it.copy(hasPendingDraft = !draftPrefs.isEmpty()) }
    }

    private fun observeCases() {
        viewModelScope.launch {
            caseRepository.cases.collect { cases ->
                _uiState.update { it.copy(activeCases = cases) }
            }
        }
    }

    private fun fetchUserLocation() {
        locationTokenSource = fetchCurrentLocation { location ->
            _uiState.update { it.copy(userLat = location?.first, userLng = location?.second) }
            if (location != null) AppContainer.lastLocationPrefs.save(location.first, location.second)
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                if (location != null) {
                    caseRepository.refreshCercanosConFallback(
                        location.first,
                        location.second,
                        _uiState.value.radiusKm.toDouble()
                    )
                } else {
                    caseRepository.refresh()
                }
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onEvent(event: CitizenHomeEvent) {
        when (event) {
            CitizenHomeEvent.OpenMapClicked ->
                viewModelScope.launch { _effect.emit(CitizenHomeEffect.NavigateToMap) }

            CitizenHomeEvent.CaptureEvidenceClicked -> {
                if (!draftPrefs.isEmpty()) {
                    viewModelScope.launch { _effect.emit(CitizenHomeEffect.NavigateToConfirmReport) }
                } else {
                    viewModelScope.launch { _effect.emit(CitizenHomeEffect.NavigateToCamera) }
                }
            }

            CitizenHomeEvent.RefreshClicked -> {
                _searchQuery.value = ""
                _uiState.update { it.copy(searchQuery = "", selectedCase = null, searchResults = null, isSearching = false) }
                fetchUserLocation()
            }

            CitizenHomeEvent.DismissCaseModal ->
                _uiState.update { it.copy(selectedCase = null) }

            is CitizenHomeEvent.SearchQueryChanged -> {
                _searchQuery.value = event.query
                _uiState.update { it.copy(searchQuery = event.query) }
                if (event.query.isBlank()) {
                    _uiState.update { it.copy(searchResults = null, isSearching = false) }
                }
            }

            is CitizenHomeEvent.CaseCardClicked -> {
                val isFromSearch = _uiState.value.searchResults?.any { it.id == event.case.id } == true
                _uiState.update { it.copy(selectedCase = event.case) }
                if (isFromSearch) viewModelScope.launch { caseRepository.cacheCase(event.case) }
            }

            is CitizenHomeEvent.RadiusChanged ->
                _uiState.update { it.copy(radiusKm = event.km) }

            CitizenHomeEvent.ResumeDraftClicked ->
                viewModelScope.launch { _effect.emit(CitizenHomeEffect.NavigateToConfirmReport) }

            CitizenHomeEvent.ScreenResumed -> checkPendingDraft()

            is CitizenHomeEvent.CaseSelectedForReport -> {
                CapturedEvidenceHolder.preselectedCaseId = event.caseId
                viewModelScope.launch { _effect.emit(CitizenHomeEffect.NavigateToCamera) }
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
                        _uiState.update { it.copy(isSearching = true) }
                        try {
                            _uiState.update { it.copy(searchResults = caseRepository.searchByName(query), isSearching = false) }
                        } catch (_: Exception) {
                            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
                        }
                    } else {
                        _uiState.update { it.copy(searchResults = null, isSearching = false) }
                    }
                }
        }
    }
}
