package com.trobat.ui.capture

import android.net.Uri
import io.mockk.mockk
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
class CaptureEvidenceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: CaptureEvidenceViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        CapturedEvidenceHolder.photoUri = null
        CapturedEvidenceHolder.latitude = null
        CapturedEvidenceHolder.longitude = null
        CapturedEvidenceHolder.preselectedCaseId = null
        CapturedEvidenceHolder.localFilePath = null
        viewModel = CaptureEvidenceViewModel()
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

    // Initial state

    @Test
    fun `initial state has no photo and no permissions`() {
        val state = viewModel.uiState.value
        assertFalse(state.hasPhoto)
        assertFalse(state.hasCameraPermission)
        assertFalse(state.hasLocationPermission)
        assertFalse(state.isCapturing)
        assertNull(state.errorMessage)
    }

    // PermissionsChanged

    @Test
    fun `PermissionsChanged with both true updates hasRequiredPermissions`() {
        viewModel.onEvent(CaptureEvidenceEvent.PermissionsChanged(
            hasCameraPermission = true, hasLocationPermission = true
        ))
        val state = viewModel.uiState.value
        assertTrue(state.hasCameraPermission)
        assertTrue(state.hasLocationPermission)
        assertTrue(state.hasRequiredPermissions)
        assertNull(state.errorMessage)
    }

    @Test
    fun `PermissionsChanged with only camera clears error and sets camera true`() {
        viewModel.onEvent(CaptureEvidenceEvent.PermissionsChanged(
            hasCameraPermission = true, hasLocationPermission = false
        ))
        val state = viewModel.uiState.value
        assertTrue(state.hasCameraPermission)
        assertFalse(state.hasLocationPermission)
        assertFalse(state.hasRequiredPermissions)
    }

    // TakePhotoClicked

    @Test
    fun `TakePhotoClicked without permissions sets error message`() {
        viewModel.onEvent(CaptureEvidenceEvent.TakePhotoClicked)
        assertNotNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isCapturing)
    }

    @Test
    fun `TakePhotoClicked with all permissions sets isCapturing true`() {
        viewModel.onEvent(CaptureEvidenceEvent.PermissionsChanged(true, true))
        viewModel.onEvent(CaptureEvidenceEvent.TakePhotoClicked)
        assertTrue(viewModel.uiState.value.isCapturing)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `TakePhotoClicked with only location permission sets error`() {
        viewModel.onEvent(CaptureEvidenceEvent.PermissionsChanged(false, true))
        viewModel.onEvent(CaptureEvidenceEvent.TakePhotoClicked)
        assertNotNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isCapturing)
    }

    // PhotoCaptured

    @Test
    fun `PhotoCaptured with coords sets hasPhoto true and no error`() {
        val uri = mockk<Uri>(relaxed = true)
        viewModel.onEvent(CaptureEvidenceEvent.PhotoCaptured(uri, -34.0, -58.0))
        val state = viewModel.uiState.value
        assertTrue(state.hasPhoto)
        assertFalse(state.isCapturing)
        assertEquals(uri, state.capturedPhotoUri)
        assertEquals(-34.0, state.capturedLatitude)
        assertEquals(-58.0, state.capturedLongitude)
        assertNull(state.errorMessage)
    }

    @Test
    fun `PhotoCaptured without coords sets hasPhoto true but shows location error`() {
        val uri = mockk<Uri>(relaxed = true)
        viewModel.onEvent(CaptureEvidenceEvent.PhotoCaptured(uri, null, null))
        val state = viewModel.uiState.value
        assertTrue(state.hasPhoto)
        assertFalse(state.hasLocationData)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `PhotoCaptured stores evidence in CapturedEvidenceHolder`() {
        val uri = mockk<Uri>(relaxed = true)
        viewModel.onEvent(CaptureEvidenceEvent.PhotoCaptured(uri, -34.0, -58.0))
        assertEquals(uri, CapturedEvidenceHolder.photoUri)
        assertEquals(-34.0, CapturedEvidenceHolder.latitude)
        assertEquals(-58.0, CapturedEvidenceHolder.longitude)
    }

    // UseEvidenceClicked

    @Test
    fun `UseEvidenceClicked without photo sets error`() = runTest {
        viewModel.onEvent(CaptureEvidenceEvent.UseEvidenceClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `UseEvidenceClicked with photo but no location sets error`() = runTest {
        val uri = mockk<Uri>(relaxed = true)
        viewModel.onEvent(CaptureEvidenceEvent.PhotoCaptured(uri, null, null))
        viewModel.onEvent(CaptureEvidenceEvent.UseEvidenceClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `UseEvidenceClicked with photo and location emits NavigateToConfirmReport`() = runTest {
        val uri = mockk<Uri>(relaxed = true)
        viewModel.onEvent(CaptureEvidenceEvent.PhotoCaptured(uri, -34.0, -58.0))

        val effects = mutableListOf<CaptureEvidenceEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.onEvent(CaptureEvidenceEvent.UseEvidenceClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(effects.contains(CaptureEvidenceEffect.NavigateToConfirmReport))
        job.cancel()
    }

    // RetakePhotoClicked

    @Test
    fun `RetakePhotoClicked resets state`() {
        val uri = mockk<Uri>(relaxed = true)
        viewModel.onEvent(CaptureEvidenceEvent.PhotoCaptured(uri, -34.0, -58.0))
        viewModel.onEvent(CaptureEvidenceEvent.RetakePhotoClicked)
        val state = viewModel.uiState.value
        assertFalse(state.hasPhoto)
        assertFalse(state.isCapturing)
        assertNull(state.capturedPhotoUri)
        assertNull(state.capturedLatitude)
        assertNull(state.capturedLongitude)
        assertNull(state.errorMessage)
    }

    @Test
    fun `RetakePhotoClicked clears CapturedEvidenceHolder`() {
        val uri = mockk<Uri>(relaxed = true)
        viewModel.onEvent(CaptureEvidenceEvent.PhotoCaptured(uri, -34.0, -58.0))
        viewModel.onEvent(CaptureEvidenceEvent.RetakePhotoClicked)
        assertNull(CapturedEvidenceHolder.photoUri)
        assertNull(CapturedEvidenceHolder.latitude)
        assertNull(CapturedEvidenceHolder.longitude)
    }

    // CaptureError

    @Test
    fun `CaptureError sets error message and stops capturing`() {
        viewModel.onEvent(CaptureEvidenceEvent.PermissionsChanged(true, true))
        viewModel.onEvent(CaptureEvidenceEvent.TakePhotoClicked)
        viewModel.onEvent(CaptureEvidenceEvent.CaptureError("Camera failed"))
        val state = viewModel.uiState.value
        assertFalse(state.isCapturing)
        assertEquals("Camera failed", state.errorMessage)
    }

    @Test
    fun `CaptureError with null message uses default error text`() {
        viewModel.onEvent(CaptureEvidenceEvent.CaptureError(null))
        assertNotNull(viewModel.uiState.value.errorMessage)
    }
}
