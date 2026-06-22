package com.trobat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trobat.data.repository.CitizenReportRepository
import com.trobat.data.repository.NotificationRepository
import com.trobat.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val reportRepository: CitizenReportRepository =
        RepositoryProvider.citizenReportRepository
    private val notificationRepository: NotificationRepository =
        RepositoryProvider.notificationRepository

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
                notificationRepository.observeUnreadCount(),
                reportRepository.pendingReports
            ) { alerts, unread, pending ->
                NotificationsUiState(
                    alerts = alerts,
                    pendingReports = pending,
                    unreadCount = unread
                )
            }.collect { _uiState.value = it }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            _uiState.value.alerts
                .filter { !it.isRead }
                .forEach { notificationRepository.markAsRead(it.id) }
        }
    }
}
