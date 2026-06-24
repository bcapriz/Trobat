package com.trobat.ui.report

import android.app.Application
import android.net.Uri
import com.trobat.data.local.ReportDraftPrefs
import com.trobat.data.repository.CitizenReportRepository
import com.trobat.data.repository.FakeCaseRepository
import com.trobat.helpers.FakeAuthRepository
import com.trobat.helpers.setAppContainerField
import com.trobat.ui.capture.CapturedEvidenceHolder
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmReportViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockDraftPrefs: ReportDraftPrefs
    private lateinit var mockApp: Application
    private lateinit var mockReportRepo: CitizenReportRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockApp = mockk(relaxed = true)
        mockDraftPrefs = mockk(relaxed = true)
        every { mockDraftPrefs.isEmpty() } returns true
        every { mockDraftPrefs.load() } returns ReportDraftPrefs.Draft()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        CapturedEvidenceHolder.photoUri = null
        CapturedEvidenceHolder.latitude = null
        CapturedEvidenceHolder.longitude = null
        CapturedEvidenceHolder.preselectedCaseId = null
        CapturedEvidenceHolder.localFilePath = null
    }

    private fun buildViewModel(
        sendReportResult: Boolean = true
    ): ConfirmReportViewModel {
        mockReportRepo = mockk(relaxed = true)
        coEvery { mockReportRepo.sendReport(any(), any(), any()) } returns sendReportResult
        coEvery { mockReportRepo.cleanupSentReports() } just Runs

        setAppContainerField("citizenReportRepository", mockReportRepo)
        setAppContainerField("caseRepository", FakeCaseRepository())
        setAppContainerField("authRepository", FakeAuthRepository())
        setAppContainerField("reportDraftPrefs", mockDraftPrefs)
        return ConfirmReportViewModel(mockApp)
    }

    private fun buildViewModelWithEvidence(
        lat: Double = -34.0,
        lng: Double = -58.0,
        sendReportResult: Boolean = true
    ): ConfirmReportViewModel {
        CapturedEvidenceHolder.photoUri = mockk<Uri>(relaxed = true)
        CapturedEvidenceHolder.latitude = lat
        CapturedEvidenceHolder.longitude = lng
        CapturedEvidenceHolder.localFilePath = null
        return buildViewModel(sendReportResult)
    }

    // Initial state without evidence

    @Test
    fun `initial state with no evidence has null lat lng`() {
        val viewModel = buildViewModel()
        val state = viewModel.uiState.value
        assertNull(state.latitude)
        assertNull(state.longitude)
        assertNull(state.photoUri)
        assertFalse(state.isSending)
    }

    // Initial state with evidence

    @Test
    fun `initial state with captured evidence has lat lng and photo`() {
        val viewModel = buildViewModelWithEvidence(lat = -34.5, lng = -58.5)
        val state = viewModel.uiState.value
        assertEquals(-34.5, state.latitude)
        assertEquals(-58.5, state.longitude)
        assertNotNull(state.photoUri)
    }

    @Test
    fun `canSendReport is false without case or description`() {
        val viewModel = buildViewModelWithEvidence()
        assertFalse(viewModel.uiState.value.canSendReport)
    }

    // Validation errors

    @Test
    fun `SendReportClicked with no case shows case error`() = runTest {
        val viewModel = buildViewModelWithEvidence()
        testDispatcher.scheduler.advanceUntilIdle() // let observeCases populate

        viewModel.onEvent(ConfirmReportEvent.RequiredDescriptionChanged("Avistamiento en parque"))
        viewModel.onEvent(ConfirmReportEvent.SendReportClicked)

        assertTrue(viewModel.uiState.value.showCaseError)
        assertFalse(viewModel.uiState.value.isSending)
    }

    @Test
    fun `SendReportClicked with no description shows description error`() = runTest {
        val viewModel = buildViewModelWithEvidence()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ConfirmReportEvent.CaseSelected("1"))
        viewModel.onEvent(ConfirmReportEvent.SendReportClicked)

        assertTrue(viewModel.uiState.value.showRequiredDescriptionError)
        assertFalse(viewModel.uiState.value.isSending)
    }

    @Test
    fun `SendReportClicked with no location is silently blocked`() = runTest {
        val viewModel = buildViewModel() // no evidence → no lat/lng
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ConfirmReportEvent.CaseSelected("1"))
        viewModel.onEvent(ConfirmReportEvent.RequiredDescriptionChanged("Avistamiento"))
        viewModel.onEvent(ConfirmReportEvent.SendReportClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        // No location → silently blocked, no sending starts
        assertFalse(viewModel.uiState.value.isSending)
    }

    // Successful send

    @Test
    fun `successful send emits NavigateToHeatMap`() = runTest {
        val viewModel = buildViewModelWithEvidence(sendReportResult = true)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ConfirmReportEvent.CaseSelected("1"))
        viewModel.onEvent(ConfirmReportEvent.RequiredDescriptionChanged("Avistamiento en parque"))

        val effects = mutableListOf<ConfirmReportEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.onEvent(ConfirmReportEvent.SendReportClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(effects.contains(ConfirmReportEffect.NavigateToHeatMap))
        assertFalse(viewModel.uiState.value.isSending)
        job.cancel()
    }

    // Failed send (saved locally)

    @Test
    fun `failed send emits ReportSavedLocally`() = runTest {
        val viewModel = buildViewModelWithEvidence(sendReportResult = false)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ConfirmReportEvent.CaseSelected("1"))
        viewModel.onEvent(ConfirmReportEvent.RequiredDescriptionChanged("Avistamiento"))

        val effects = mutableListOf<ConfirmReportEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.onEvent(ConfirmReportEvent.SendReportClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(effects.contains(ConfirmReportEffect.ReportSavedLocally))
        job.cancel()
    }

    // Retake photo

    @Test
    fun `RetakePhotoClicked emits NavigateBackToCamera and clears holder`() = runTest {
        val viewModel = buildViewModelWithEvidence()
        val effects = mutableListOf<ConfirmReportEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.onEvent(ConfirmReportEvent.RetakePhotoClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(effects.contains(ConfirmReportEffect.NavigateBackToCamera))
        assertNull(CapturedEvidenceHolder.photoUri)
        job.cancel()
    }

    // Cancel

    @Test
    fun `CancelClicked emits NavigateToCases and clears holder`() = runTest {
        val viewModel = buildViewModelWithEvidence()
        val effects = mutableListOf<ConfirmReportEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.onEvent(ConfirmReportEvent.CancelClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(effects.contains(ConfirmReportEffect.NavigateToCases))
        assertNull(CapturedEvidenceHolder.photoUri)
        job.cancel()
    }

    // Case selection

    @Test
    fun `CaseSelected updates selectedCaseId and clears case error`() = runTest {
        val viewModel = buildViewModelWithEvidence()
        testDispatcher.scheduler.advanceUntilIdle() // populate activeCases

        viewModel.onEvent(ConfirmReportEvent.SendReportClicked) // trigger showCaseError
        viewModel.onEvent(ConfirmReportEvent.CaseSelected("1"))

        assertEquals("1", viewModel.uiState.value.selectedCaseId)
        assertFalse(viewModel.uiState.value.showCaseError)
    }

    // Identification toggle

    @Test
    fun `IdentificationToggled to true sets isIdentified`() {
        val viewModel = buildViewModel()
        viewModel.onEvent(ConfirmReportEvent.IdentificationToggled(true))
        assertTrue(viewModel.uiState.value.isIdentified)
    }

    @Test
    fun `IdentificationToggled to false clears isIdentified`() {
        val viewModel = buildViewModel()
        viewModel.onEvent(ConfirmReportEvent.IdentificationToggled(true))
        viewModel.onEvent(ConfirmReportEvent.IdentificationToggled(false))
        assertFalse(viewModel.uiState.value.isIdentified)
    }

    // Description validation

    @Test
    fun `RequiredDescriptionChanged clears error when non-blank`() {
        val viewModel = buildViewModelWithEvidence()
        viewModel.onEvent(ConfirmReportEvent.SendReportClicked) // trigger error
        viewModel.onEvent(ConfirmReportEvent.RequiredDescriptionChanged("Avistamiento"))
        assertFalse(viewModel.uiState.value.showRequiredDescriptionError)
    }

    @Test
    fun `RequiredDescriptionChanged to blank sets description error`() {
        val viewModel = buildViewModel()
        viewModel.onEvent(ConfirmReportEvent.RequiredDescriptionChanged("algo"))
        viewModel.onEvent(ConfirmReportEvent.RequiredDescriptionChanged(""))
        assertTrue(viewModel.uiState.value.showRequiredDescriptionError)
    }

    // canSendReport

    @Test
    fun `canSendReport true when case description and location all set`() = runTest {
        val viewModel = buildViewModelWithEvidence()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ConfirmReportEvent.CaseSelected("1"))
        viewModel.onEvent(ConfirmReportEvent.RequiredDescriptionChanged("Avistamiento"))
        assertTrue(viewModel.uiState.value.canSendReport)
    }

    // Duplicate send guard

    @Test
    fun `SendReportClicked while isSending is a no-op`() = runTest {
        val viewModel = buildViewModelWithEvidence()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ConfirmReportEvent.CaseSelected("1"))
        viewModel.onEvent(ConfirmReportEvent.RequiredDescriptionChanged("Avistamiento"))

        // Send once but don't advance → isSending = true
        viewModel.onEvent(ConfirmReportEvent.SendReportClicked)
        // Send again immediately → should be no-op (isSending guard)
        viewModel.onEvent(ConfirmReportEvent.SendReportClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        // Should still complete normally (only one send happened)
        assertFalse(viewModel.uiState.value.isSending)
    }
}
