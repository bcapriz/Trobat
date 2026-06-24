package com.trobat.ui.splash

import com.trobat.data.local.OnboardingPrefs
import com.trobat.helpers.FakeAuthRepository
import com.trobat.helpers.setAppContainerField
import io.mockk.every
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuth: FakeAuthRepository
    private lateinit var mockOnboarding: OnboardingPrefs

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockOnboarding = mockk(relaxed = true)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        loggedIn: Boolean = false,
        hasSeenOnboarding: Boolean = false
    ): SplashViewModel {
        fakeAuth = FakeAuthRepository(loggedIn = loggedIn)
        every { mockOnboarding.hasSeenOnboarding } returns hasSeenOnboarding
        setAppContainerField("authRepository", fakeAuth)
        setAppContainerField("onboardingPrefs", mockOnboarding)
        return SplashViewModel()
    }

    // Navigation routing

    @Test
    fun `navigates to main when user is already logged in`() = runTest {
        val viewModel = buildViewModel(loggedIn = true)
        val effects = mutableListOf<SplashEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.startSplash()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(effects.contains(SplashEffect.NavigateToMain))
        job.cancel()
    }

    @Test
    fun `navigates to onboarding when not logged in and onboarding not seen`() = runTest {
        val viewModel = buildViewModel(loggedIn = false, hasSeenOnboarding = false)
        val effects = mutableListOf<SplashEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.startSplash()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(effects.contains(SplashEffect.NavigateToOnboarding))
        job.cancel()
    }

    @Test
    fun `navigates to login when not logged in but onboarding already seen`() = runTest {
        val viewModel = buildViewModel(loggedIn = false, hasSeenOnboarding = true)
        val effects = mutableListOf<SplashEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.startSplash()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(effects.contains(SplashEffect.NavigateToLogin))
        job.cancel()
    }

    @Test
    fun `logged in user skips onboarding check`() = runTest {
        val viewModel = buildViewModel(loggedIn = true, hasSeenOnboarding = false)
        val effects = mutableListOf<SplashEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.startSplash()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(effects.contains(SplashEffect.NavigateToOnboarding))
        assertTrue(effects.contains(SplashEffect.NavigateToMain))
        job.cancel()
    }

    // showLoading state

    @Test
    fun `showLoading becomes true after first delay`() = runTest {
        val viewModel = buildViewModel()
        viewModel.startSplash()

        // Advance past first 1800ms delay
        testDispatcher.scheduler.advanceTimeBy(1801)
        assertTrue(viewModel.uiState.value.showLoading)
    }

    @Test
    fun `showLoading is false before first delay`() = runTest {
        val viewModel = buildViewModel()
        viewModel.startSplash()

        testDispatcher.scheduler.advanceTimeBy(500)
        assertFalse(viewModel.uiState.value.showLoading)
    }

    // Guard against double-start

    @Test
    fun `startSplash called twice only emits one effect`() = runTest {
        val viewModel = buildViewModel(loggedIn = false, hasSeenOnboarding = true)
        val effects = mutableListOf<SplashEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.startSplash()
        viewModel.startSplash() // second call should be ignored
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, effects.size)
        job.cancel()
    }
}
