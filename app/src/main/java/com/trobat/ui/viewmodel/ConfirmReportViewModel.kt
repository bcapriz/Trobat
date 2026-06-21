package com.trobat.ui.viewmodel

import android.app.Application
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trobat.data.model.CitizenReport
import com.trobat.data.model.CapturedEvidenceHolder
import com.trobat.data.model.ReportStatus
import com.trobat.data.repository.CaseRepository
import com.trobat.data.repository.CitizenReportRepository
import com.trobat.data.repository.RepositoryProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class ConfirmReportViewModel(app: Application) : AndroidViewModel(app) {

    private val reportRepository: CitizenReportRepository = RepositoryProvider.citizenReportRepository
    private val caseRepository: CaseRepository = RepositoryProvider.caseRepository

    private val _uiState = MutableStateFlow(ConfirmReportUiState())
    val uiState: StateFlow<ConfirmReportUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ConfirmReportEffect>()
    val effect: SharedFlow<ConfirmReportEffect> = _effect.asSharedFlow()

    init {
        observeCases()
        loadCapturedEvidence()
    }

    private fun observeCases() {
        viewModelScope.launch {
            caseRepository.cases.collect { cases ->
                _uiState.value = _uiState.value.copy(activeCases = cases)
            }
        }
    }

    private fun loadCapturedEvidence() {
        val lat = CapturedEvidenceHolder.latitude
        val lng = CapturedEvidenceHolder.longitude
        _uiState.value = _uiState.value.copy(
            photoUri = CapturedEvidenceHolder.photoUri,
            latitude = lat,
            longitude = lng,
            locationLabel = if (lat != null && lng != null) "Obteniendo dirección..." else "Ubicación no disponible"
        )
        if (lat != null && lng != null) {
            viewModelScope.launch {
                val label = reverseGeocode(lat, lng)
                _uiState.value = _uiState.value.copy(locationLabel = label)
            }
        }
    }

    private suspend fun reverseGeocode(lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(getApplication(), Locale.getDefault())
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(lat, lng, 1) { list -> cont.resume(list) }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(lat, lng, 1) ?: emptyList()
            }
            addresses.firstOrNull()?.let { addr ->
                buildString {
                    addr.thoroughfare?.let { append(it) }
                    addr.subThoroughfare?.let { if (isNotEmpty()) append(" $it") else append(it) }
                    addr.locality?.let { if (isNotEmpty()) append(", $it") else append(it) }
                    addr.adminArea?.let { if (isNotEmpty()) append(", $it") else append(it) }
                }.ifBlank { "${"%.5f".format(lat)}, ${"%.5f".format(lng)}" }
            } ?: "${"%.5f".format(lat)}, ${"%.5f".format(lng)}"
        } catch (_: Exception) {
            "${"%.5f".format(lat)}, ${"%.5f".format(lng)}"
        }
    }

    fun onEvent(event: ConfirmReportEvent) {
        when (event) {
            is ConfirmReportEvent.CaseSelected -> onCaseSelected(event.caseId)
            is ConfirmReportEvent.RequiredDescriptionChanged -> onRequiredDescriptionChanged(event.value)
            is ConfirmReportEvent.OptionalDetailsChanged -> onOptionalDetailsChanged(event.value)
            ConfirmReportEvent.SendReportClicked -> sendReport()
            ConfirmReportEvent.RetakePhotoClicked -> retakePhoto()
            is ConfirmReportEvent.IdentificationToggled -> _uiState.value =
                _uiState.value.copy(isIdentified = event.isIdentified)
        }
    }

    private fun onCaseSelected(caseId: String) {
        _uiState.value = _uiState.value.copy(selectedCaseId = caseId, showCaseError = false)
    }

    private fun onRequiredDescriptionChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            requiredDescription = value,
            showRequiredDescriptionError = value.isBlank()
        )
    }

    private fun onOptionalDetailsChanged(value: String) {
        _uiState.value = _uiState.value.copy(optionalDetails = value)
    }

    private fun sendReport() {
        val currentState = _uiState.value
        val hasCaseError = currentState.selectedCaseId == null
        val hasDescriptionError = currentState.requiredDescription.isBlank()

        if (hasCaseError || hasDescriptionError) {
            _uiState.value = currentState.copy(
                showCaseError = hasCaseError,
                showRequiredDescriptionError = hasDescriptionError
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isSending = true)

            val newReport = CitizenReport(
                id = System.currentTimeMillis().toString(),
                caseId = currentState.selectedCaseId ?: "",
                title = "Reporte ciudadano",
                description = currentState.requiredDescription,
                optionalDetails = currentState.optionalDetails.ifBlank { null },
                address = currentState.locationLabel,
                createdAt = "Ahora",
                latitude = currentState.latitude ?: -34.6037,
                longitude = currentState.longitude ?: -58.3816,
                status = ReportStatus.SENT
            )

            reportRepository.sendReport(newReport)
            CapturedEvidenceHolder.clear()
            _uiState.value = _uiState.value.copy(isSending = false)
            _effect.emit(ConfirmReportEffect.NavigateToHeatMap)
        }
    }

    private fun retakePhoto() {
        CapturedEvidenceHolder.clear()
        viewModelScope.launch { _effect.emit(ConfirmReportEffect.NavigateBackToCamera) }
    }
}
