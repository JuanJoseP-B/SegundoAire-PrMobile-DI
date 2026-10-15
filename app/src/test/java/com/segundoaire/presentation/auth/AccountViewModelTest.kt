package com.segundoaire.presentation.auth

import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.usecase.SaveAccountUseCase
import com.segundoaire.testing.FakeUserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val profiles = FakeUserProfileRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = AccountViewModel(SaveAccountUseCase(profiles))

    @Test
    fun `un proveedor muestra la carga y no guarda hasta que termina`() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.onAuthModeSelected(AuthMode.GOOGLE)
        advanceTimeBy(AccountViewModel.PROVIDER_DELAY_MILLIS - 1)

        assertEquals(AuthMode.GOOGLE, viewModel.uiState.value.loadingMode)
        assertTrue(viewModel.uiState.value.isConnecting)
        assertFalse(viewModel.uiState.value.isCompleted)
        assertNull(profiles.saved)
    }

    @Test
    fun `al terminar la carga guarda el proveedor y completa`() = runTest(dispatcher) {
        listOf(AuthMode.GOOGLE, AuthMode.APPLE).forEach { mode ->
            val viewModel = viewModel()

            viewModel.onAuthModeSelected(mode)
            advanceUntilIdle()

            assertEquals(mode, profiles.saved?.authMode)
            assertTrue(viewModel.uiState.value.isCompleted)
            assertFalse(viewModel.uiState.value.isBusy)
        }
    }

    @Test
    fun `continuar sin cuenta guarda GUEST sin espera ni carga de proveedor`() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.onAuthModeSelected(AuthMode.GUEST)
        assertFalse(viewModel.uiState.value.isConnecting)
        runCurrent()

        assertEquals(AuthMode.GUEST, profiles.saved?.authMode)
        assertTrue(viewModel.uiState.value.isCompleted)
    }

    @Test
    fun `el correo no se guarda desde esta pantalla`() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.onAuthModeSelected(AuthMode.EMAIL)
        advanceUntilIdle()

        assertNull(profiles.saved)
        assertFalse(viewModel.uiState.value.isBusy)
        assertFalse(viewModel.uiState.value.isCompleted)
    }

    @Test
    fun `mientras carga se ignoran los demas toques`() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.onAuthModeSelected(AuthMode.GOOGLE)
        viewModel.onAuthModeSelected(AuthMode.APPLE)
        viewModel.onAuthModeSelected(AuthMode.GUEST)
        advanceUntilIdle()

        assertEquals(AuthMode.GOOGLE, profiles.saved?.authMode)
        assertEquals(1, profiles.saveCount)
    }

    @Test
    fun `si el guardado falla se puede reintentar`() = runTest(dispatcher) {
        val viewModel = viewModel()
        profiles.failNextSave = true

        viewModel.onAuthModeSelected(AuthMode.APPLE)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isCompleted)
        assertFalse(viewModel.uiState.value.isBusy)

        viewModel.onAuthModeSelected(AuthMode.APPLE)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isCompleted)
    }
}
