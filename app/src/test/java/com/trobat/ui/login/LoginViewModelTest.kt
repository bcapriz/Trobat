package com.trobat.ui.login

import com.trobat.helpers.FakeAuthRepository
import com.trobat.helpers.setAppContainerField
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
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuth: FakeAuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        loginResult: Result<Unit> = Result.success(Unit),
        loggedIn: Boolean = false
    ): LoginViewModel {
        fakeAuth = FakeAuthRepository(loginResult = loginResult, loggedIn = loggedIn)
        setAppContainerField("authRepository", fakeAuth)
        return LoginViewModel()
    }

    // Initial state

    @Test
    fun `initial state is empty with no error`() {
        viewModel = buildViewModel()
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    // Field updates

    @Test
    fun `EmailChanged updates email in state`() {
        viewModel = buildViewModel()
        viewModel.onEvent(LoginEvent.EmailChanged("user@test.com"))
        assertEquals("user@test.com", viewModel.uiState.value.email)
    }

    @Test
    fun `PasswordChanged updates password in state`() {
        viewModel = buildViewModel()
        viewModel.onEvent(LoginEvent.PasswordChanged("secret123"))
        assertEquals("secret123", viewModel.uiState.value.password)
    }

    @Test
    fun `EmailChanged clears previous error`() {
        viewModel = buildViewModel()
        viewModel.onEvent(LoginEvent.LoginClicked) // triggers error
        viewModel.onEvent(LoginEvent.EmailChanged("user@test.com"))
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `PasswordChanged clears previous error`() {
        viewModel = buildViewModel()
        viewModel.onEvent(LoginEvent.LoginClicked) // triggers error
        viewModel.onEvent(LoginEvent.PasswordChanged("pass"))
        assertNull(viewModel.uiState.value.error)
    }

    // Validation

    @Test
    fun `LoginClicked with blank email shows validation error`() {
        viewModel = buildViewModel()
        viewModel.onEvent(LoginEvent.PasswordChanged("secret"))
        viewModel.onEvent(LoginEvent.LoginClicked)
        assertNotNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `LoginClicked with blank password shows validation error`() {
        viewModel = buildViewModel()
        viewModel.onEvent(LoginEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(LoginEvent.LoginClicked)
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `LoginClicked with both fields blank shows validation error`() {
        viewModel = buildViewModel()
        viewModel.onEvent(LoginEvent.LoginClicked)
        assertEquals("Completá todos los campos", viewModel.uiState.value.error)
    }

    // Successful login

    @Test
    fun `successful login emits NavigateToMain effect`() = runTest {
        viewModel = buildViewModel(loginResult = Result.success(Unit))
        viewModel.onEvent(LoginEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(LoginEvent.PasswordChanged("secret"))

        val effects = mutableListOf<LoginEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.onEvent(LoginEvent.LoginClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(effects.contains(LoginEffect.NavigateToMain))
        job.cancel()
    }

    @Test
    fun `successful login clears loading state after completion`() = runTest {
        viewModel = buildViewModel(loginResult = Result.success(Unit))
        viewModel.onEvent(LoginEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(LoginEvent.PasswordChanged("secret"))
        viewModel.onEvent(LoginEvent.LoginClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // Failed login

    @Test
    fun `failed login shows error from exception message`() = runTest {
        viewModel = buildViewModel(
            loginResult = Result.failure(Exception("Credenciales inválidas"))
        )
        viewModel.onEvent(LoginEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(LoginEvent.PasswordChanged("wrong"))
        viewModel.onEvent(LoginEvent.LoginClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Credenciales inválidas", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `failed login with no message shows fallback error`() = runTest {
        viewModel = buildViewModel(loginResult = Result.failure(Exception()))
        viewModel.onEvent(LoginEvent.EmailChanged("user@test.com"))
        viewModel.onEvent(LoginEvent.PasswordChanged("wrong"))
        viewModel.onEvent(LoginEvent.LoginClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
    }

    // Navigation

    @Test
    fun `RegisterClicked emits NavigateToRegister effect`() = runTest {
        viewModel = buildViewModel()
        val effects = mutableListOf<LoginEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.onEvent(LoginEvent.RegisterClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(effects.contains(LoginEffect.NavigateToRegister))
        job.cancel()
    }
}
