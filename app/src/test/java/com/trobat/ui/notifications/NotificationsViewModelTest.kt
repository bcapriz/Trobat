package com.trobat.ui.notifications

import com.trobat.data.local.NotificationEntity
import com.trobat.data.repository.FakeCaseRepository
import com.trobat.data.repository.FakeCitizenReportRepository
import com.trobat.data.repository.NotificationRepository
import com.trobat.helpers.setAppContainerField
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockNotifRepo: NotificationRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun notif(id: Int, read: Boolean) = NotificationEntity(
        id = id, title = "Alerta", body = "Reporte cercano", receivedAt = 1000L, isRead = read
    )

    private fun buildViewModel(alerts: List<NotificationEntity> = emptyList()): NotificationsViewModel {
        mockNotifRepo = mockk()
        every { mockNotifRepo.observeAll() } returns flowOf(alerts)
        coEvery { mockNotifRepo.markAllRead() } just Runs

        setAppContainerField("notificationRepository", mockNotifRepo)
        setAppContainerField("citizenReportRepository", FakeCitizenReportRepository())
        setAppContainerField("caseRepository", FakeCaseRepository())
        return NotificationsViewModel()
    }

    // Initial state

    @Test
    fun `initial state before collection is empty`() {
        val viewModel = buildViewModel()
        val state = viewModel.uiState.value
        assertTrue(state.alerts.isEmpty())
        assertEquals(0, state.unreadCount)
    }

    @Test
    fun `state populated from repos after collection`() = runTest {
        val alerts = listOf(notif(1, read = false), notif(2, read = true))
        val viewModel = buildViewModel(alerts)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.alerts.size)
    }

    // Unread count

    @Test
    fun `unreadCount counts only unread alerts`() = runTest {
        val alerts = listOf(
            notif(1, read = false),
            notif(2, read = false),
            notif(3, read = true)
        )
        val viewModel = buildViewModel(alerts)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.unreadCount)
    }

    @Test
    fun `unreadCount is zero when all read`() = runTest {
        val alerts = listOf(notif(1, read = true), notif(2, read = true))
        val viewModel = buildViewModel(alerts)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.unreadCount)
    }

    @Test
    fun `unreadCount is zero with no alerts`() = runTest {
        val viewModel = buildViewModel(emptyList())
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.unreadCount)
    }

    // MarkAllRead

    @Test
    fun `MarkAllRead event calls markAllRead on repository`() = runTest {
        val viewModel = buildViewModel()
        viewModel.onEvent(NotificationsEvent.MarkAllRead)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { mockNotifRepo.markAllRead() }
    }

    // Pending reports

    @Test
    fun `pending reports list is included in state`() = runTest {
        // FakeCitizenReportRepository.pendingReports returns flowOf(emptyList())
        val viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.pendingReports.isEmpty())
    }
}
