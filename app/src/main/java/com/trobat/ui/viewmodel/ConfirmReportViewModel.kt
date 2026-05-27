package com.trobat.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trobat.data.model.ActiveCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.trobat.data.model.CitizenReport
import com.trobat.data.model.ReportStatus
import com.trobat.data.repository.CitizenReportRepository
import com.trobat.data.repository.RepositoryProvider
class ConfirmReportViewModel : ViewModel() {

    private val reportRepository: CitizenReportRepository = RepositoryProvider.citizenReportRepository
    private val _uiState = MutableStateFlow(ConfirmReportUiState())
    val uiState: StateFlow<ConfirmReportUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ConfirmReportEffect>()
    val effect: SharedFlow<ConfirmReportEffect> = _effect.asSharedFlow()

    init {
        loadActiveCases()
    }

    private fun loadActiveCases() {
        val fakeCases = listOf(
            ActiveCase(
                id = "1",
                title = "Búsqueda activa - Zona Palermo",
                personName = "Juan Pérez",
                area = "Palermo, CABA"
            ),
            ActiveCase(
                id = "2",
                title = "Búsqueda activa - Zona Once",
                personName = "María Gómez",
                area = "Once, CABA"
            ),
            ActiveCase(
                id = "3",
                title = "Búsqueda activa - Zona Caballito",
                personName = "Lucas Fernández",
                area = "Caballito, CABA"
            )
        )

        _uiState.value = _uiState.value.copy(
            activeCases = fakeCases
        )
    }

    fun onEvent(event: ConfirmReportEvent) {
        when (event) {
            is ConfirmReportEvent.CaseSelected -> {
                onCaseSelected(event.caseId)
            }

            is ConfirmReportEvent.RequiredDescriptionChanged -> {
                onRequiredDescriptionChanged(event.value)
            }

            is ConfirmReportEvent.OptionalDetailsChanged -> {
                onOptionalDetailsChanged(event.value)
            }

            ConfirmReportEvent.SendReportClicked -> {
                sendReport()
            }

            ConfirmReportEvent.RetakePhotoClicked -> {
                retakePhoto()
            }
        }
    }

    private fun onCaseSelected(caseId: String) {
        _uiState.value = _uiState.value.copy(
            selectedCaseId = caseId,
            showCaseError = false
        )
    }

    private fun onRequiredDescriptionChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            requiredDescription = value,
            showRequiredDescriptionError = value.isBlank()
        )
    }

    private fun onOptionalDetailsChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            optionalDetails = value
        )
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
            _uiState.value = currentState.copy(
                isSending = true
            )

            val newReport = CitizenReport(
                id = System.currentTimeMillis().toString(),
                caseId = currentState.selectedCaseId ?: "",
                title = "Reporte ciudadano",
                description = currentState.requiredDescription,
                optionalDetails = currentState.optionalDetails.ifBlank { null },
                address = "Av. Siempre Viva, CABA",
                createdAt = "Ahora",
                latitude = -34.6037,
                longitude = -58.3816,
                status = ReportStatus.SENT
            )

            reportRepository.sendReport(newReport)

            _uiState.value = _uiState.value.copy(
                isSending = false
            )

            _effect.emit(ConfirmReportEffect.NavigateToHeatMap)
        }
    }

    private fun retakePhoto() {
        viewModelScope.launch {
            _effect.emit(ConfirmReportEffect.NavigateBackToCamera)
        }
    }
}