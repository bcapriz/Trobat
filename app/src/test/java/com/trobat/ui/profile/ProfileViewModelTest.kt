package com.trobat.ui.profile

import com.trobat.helpers.FakeAuthRepository
import com.trobat.helpers.FakeUserPreferencesRepository
import com.trobat.helpers.setAppContainerField
import com.trobat.ui.theme.ThemeManager
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuth: FakeAuthRepository
    private lateinit var fakePrefs: FakeUserPreferencesRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        ThemeManager.setDarkMode(false) // reset before each test
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        userName: String = "Franco",
        email: String = "franco@test.com",
        nationalId: String = "12345678",
        phone: String = "1155551234",
        notificationsEnabled: Boolean = true,
        darkModeEnabled: Boolean = false
    ): ProfileViewModel {
        fakeAuth = FakeAuthRepository(
            userName = userName,
            email = email,
            nationalId = nationalId,
            phone = phone
        )
        fakePrefs = FakeUserPreferencesRepository(
            notificationsEnabled = notificationsEnabled,
            darkModeEnabled = darkModeEnabled
        )
        setAppContainerField("authRepository", fakeAuth)
        setAppContainerField("userPreferencesRepository", fakePrefs)
        return ProfileViewModel()
    }

    // Initial state (loaded on Dispatchers.IO — needs brief wait)

    @Test
    fun `initial state loads user data from repository`() {
        val viewModel = buildViewModel(
            userName = "Franco",
            email = "franco@test.com",
            nationalId = "12345678",
            phone = "1155551234"
        )
        Thread.sleep(100) // let Dispatchers.IO coroutine complete
        val state = viewModel.uiState.value
        assertEquals("Franco", state.name)
        assertEquals("franco@test.com", state.email)
        assertEquals("12345678", state.nationalId)
        assertEquals("1155551234", state.phone)
    }

    @Test
    fun `initial state loads preferences`() {
        val viewModel = buildViewModel(notificationsEnabled = false, darkModeEnabled = true)
        Thread.sleep(100)
        val state = viewModel.uiState.value
        assertFalse(state.notificationsEnabled)
        assertTrue(state.darkModeEnabled)
    }

    // Notifications toggle

    @Test
    fun `NotificationsToggled to false updates state and prefs`() {
        val viewModel = buildViewModel(notificationsEnabled = true)
        viewModel.onEvent(ProfileEvent.NotificationsToggled(false))
        assertFalse(viewModel.uiState.value.notificationsEnabled)
        assertFalse(fakePrefs.getNotificationsEnabled())
    }

    @Test
    fun `NotificationsToggled to true updates state and prefs`() {
        val viewModel = buildViewModel(notificationsEnabled = false)
        viewModel.onEvent(ProfileEvent.NotificationsToggled(true))
        assertTrue(viewModel.uiState.value.notificationsEnabled)
        assertTrue(fakePrefs.getNotificationsEnabled())
    }

    // Dark mode toggle

    @Test
    fun `DarkModeToggled to true updates state prefs and ThemeManager`() {
        val viewModel = buildViewModel(darkModeEnabled = false)
        viewModel.onEvent(ProfileEvent.DarkModeToggled(true))
        assertTrue(viewModel.uiState.value.darkModeEnabled)
        assertTrue(fakePrefs.getDarkModeEnabled())
        assertTrue(ThemeManager.darkMode.value)
    }

    @Test
    fun `DarkModeToggled to false updates state prefs and ThemeManager`() {
        val viewModel = buildViewModel(darkModeEnabled = true)
        ThemeManager.setDarkMode(true)
        viewModel.onEvent(ProfileEvent.DarkModeToggled(false))
        assertFalse(viewModel.uiState.value.darkModeEnabled)
        assertFalse(fakePrefs.getDarkModeEnabled())
        assertFalse(ThemeManager.darkMode.value)
    }

    // Logout

    @Test
    fun `LogoutClicked calls logout on authRepository`() = runTest {
        val viewModel = buildViewModel()
        viewModel.onEvent(ProfileEvent.LogoutClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(fakeAuth.logoutCalled)
    }

    @Test
    fun `LogoutClicked emits NavigateToLogin effect`() = runTest {
        val viewModel = buildViewModel()
        val effects = mutableListOf<ProfileEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.onEvent(ProfileEvent.LogoutClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(effects.contains(ProfileEffect.NavigateToLogin))
        job.cancel()
    }
}
