package com.trobat.ui.register

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
class RegisterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuth: FakeAuthRepository
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        registerResult: Result<Unit> = Result.success(Unit)
    ): RegisterViewModel {
        fakeAuth = FakeAuthRepository(registerResult = registerResult)
        setAppContainerField("authRepository", fakeAuth)
        return RegisterViewModel()
    }

    private fun fillAllFields(vm: RegisterViewModel) {
        vm.onEvent(RegisterEvent.NameChanged("Juan Pérez"))
        vm.onEvent(RegisterEvent.EmailChanged("juan@test.com"))
        vm.onEvent(RegisterEvent.PasswordChanged("password123"))
        vm.onEvent(RegisterEvent.NationalIdChanged("12345678"))
        vm.onEvent(RegisterEvent.PhoneChanged("1155551234"))
    }

    // Initial state

    @Test
    fun `initial state is empty with no error`() {
        viewModel = buildViewModel()
        val state = viewModel.uiState.value
        assertEquals("", state.name)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.nationalId)
        assertEquals("", state.phone)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    // Field changes

    @Test
    fun `NameChanged updates state and clears error`() {
        viewModel = buildViewModel()
        viewModel.onEvent(RegisterEvent.RegisterClicked) // trigger error
        viewModel.onEvent(RegisterEvent.NameChanged("Ana"))
        assertEquals("Ana", viewModel.uiState.value.name)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `EmailChanged updates state`() {
        viewModel = buildViewModel()
        viewModel.onEvent(RegisterEvent.EmailChanged("user@test.com"))
        assertEquals("user@test.com", viewModel.uiState.value.email)
    }

    @Test
    fun `PasswordChanged updates state`() {
        viewModel = buildViewModel()
        viewModel.onEvent(RegisterEvent.PasswordChanged("mypass"))
        assertEquals("mypass", viewModel.uiState.value.password)
    }

    @Test
    fun `NationalIdChanged updates state`() {
        viewModel = buildViewModel()
        viewModel.onEvent(RegisterEvent.NationalIdChanged("99887766"))
        assertEquals("99887766", viewModel.uiState.value.nationalId)
    }

    @Test
    fun `PhoneChanged updates state`() {
        viewModel = buildViewModel()
        viewModel.onEvent(RegisterEvent.PhoneChanged("1122334455"))
        assertEquals("1122334455", viewModel.uiState.value.phone)
    }

    // Validation

    @Test
    fun `RegisterClicked with all blank fields shows required error`() {
        viewModel = buildViewModel()
        viewModel.onEvent(RegisterEvent.RegisterClicked)
        assertEquals("Completá todos los campos", viewModel.uiState.value.error)
    }

    @Test
    fun `RegisterClicked with blank name shows required error`() {
        viewModel = buildViewModel()
        viewModel.onEvent(RegisterEvent.EmailChanged("a@b.com"))
        viewModel.onEvent(RegisterEvent.PasswordChanged("pass123"))
        viewModel.onEvent(RegisterEvent.NationalIdChanged("12345678"))
        viewModel.onEvent(RegisterEvent.PhoneChanged("1100000000"))
        viewModel.onEvent(RegisterEvent.RegisterClicked)
        assertEquals("Completá todos los campos", viewModel.uiState.value.error)
    }

    @Test
    fun `RegisterClicked with short password shows length error`() {
        viewModel = buildViewModel()
        fillAllFields(viewModel)
        viewModel.onEvent(RegisterEvent.PasswordChanged("12345")) // only 5 chars
        viewModel.onEvent(RegisterEvent.RegisterClicked)
        assertEquals("La contraseña debe tener al menos 6 caracteres", viewModel.uiState.value.error)
    }

    @Test
    fun `RegisterClicked with exactly 6 char password passes validation`() = runTest {
        viewModel = buildViewModel()
        fillAllFields(viewModel)
        viewModel.onEvent(RegisterEvent.PasswordChanged("123456")) // exactly 6
        viewModel.onEvent(RegisterEvent.RegisterClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNull(viewModel.uiState.value.error)
    }

    // Successful register

    @Test
    fun `successful register emits NavigateToLogin effect`() = runTest {
        viewModel = buildViewModel(registerResult = Result.success(Unit))
        fillAllFields(viewModel)

        val effects = mutableListOf<RegisterEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.onEvent(RegisterEvent.RegisterClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(effects.contains(RegisterEffect.NavigateToLogin))
        job.cancel()
    }

    @Test
    fun `successful register clears loading`() = runTest {
        viewModel = buildViewModel(registerResult = Result.success(Unit))
        fillAllFields(viewModel)
        viewModel.onEvent(RegisterEvent.RegisterClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // Failed register

    @Test
    fun `failed register shows error message`() = runTest {
        viewModel = buildViewModel(registerResult = Result.failure(Exception("Email ya registrado")))
        fillAllFields(viewModel)
        viewModel.onEvent(RegisterEvent.RegisterClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Email ya registrado", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `failed register with no message shows fallback error`() = runTest {
        viewModel = buildViewModel(registerResult = Result.failure(Exception()))
        fillAllFields(viewModel)
        viewModel.onEvent(RegisterEvent.RegisterClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
    }

    // Navigation

    @Test
    fun `LoginClicked emits NavigateToLogin effect`() = runTest {
        viewModel = buildViewModel()
        val effects = mutableListOf<RegisterEffect>()
        val job = launch { viewModel.effect.collect { effects.add(it) } }

        viewModel.onEvent(RegisterEvent.LoginClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(effects.contains(RegisterEffect.NavigateToLogin))
        job.cancel()
    }
}
