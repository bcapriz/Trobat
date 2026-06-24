package com.trobat.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trobat.data.repository.AppContainer
import com.trobat.data.repository.CaseRepository
import com.trobat.data.repository.CitizenReportRepository
import com.trobat.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val reportRepository: CitizenReportRepository =
        AppContainer.citizenReportRepository
    private val notificationRepository: NotificationRepository =
        AppContainer.notificationRepository
    private val caseRepository: CaseRepository =
        AppContainer.caseRepository

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        observeData()
        viewModelScope.launch { reportRepository.cleanupSentReports() }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                notificationRepository.observeAll(),
                reportRepository.pendingReports,
                caseRepository.cases
            ) { alerts, pending, cases ->
                NotificationsUiState(
                    alerts = alerts,
                    pendingReports = pending.map { entity ->
                        PendingReportItem(
                            entity = entity,
                            caseName = cases.firstOrNull { it.id == entity.caseId }?.fullName
                        )
                    },
                    unreadCount = alerts.count { !it.isRead }
                )
            }.collect { _uiState.value = it }
        }
    }

    fun onEvent(event: NotificationsEvent) {
        when (event) {
            NotificationsEvent.MarkAllRead -> markAllRead()
        }
    }

    private fun markAllRead() {
        viewModelScope.launch {
            notificationRepository.markAllRead()
        }
    }
}
