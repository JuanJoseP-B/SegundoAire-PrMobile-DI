package com.segundoaire.presentation.auth

import androidx.lifecycle.SavedStateHandle
import com.segundoaire.domain.usecase.ValidateEmailUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
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
class ForgotPasswordViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        ForgotPasswordViewModel(savedStateHandle, ValidateEmailUseCase())

    @Test
    fun `empieza vacio, sin error y sin poder enviar`() {
        val state = viewModel().uiState.value

        assertFalse(state.canSubmit)
        assertFalse(state.showEmailError)
        assertFalse(state.isSent)
    }

    @Test
    fun `solo un correo valido deja enviar`() {
        val viewModel = viewModel()

        viewModel.onEmailChange("ana@correo")
        assertFalse(viewModel.uiState.value.canSubmit)

        viewModel.onEmailChange("ana@correo.com")
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `el error espera a que el usuario salga del campo y se quita al corregir`() {
        val viewModel = viewModel()

        viewModel.onEmailChange("ana@")
        assertFalse(viewModel.uiState.value.showEmailError)

        viewModel.onEmailBlur()
        assertTrue(viewModel.uiState.value.showEmailError)

        viewModel.onEmailChange("ana@correo.com")
        assertFalse(viewModel.uiState.value.showEmailError)
    }

    @Test
    fun `un campo vacio no muestra error aunque se haya tocado`() {
        val viewModel = viewModel()

        viewModel.onEmailBlur()

        assertFalse(viewModel.uiState.value.showEmailError)
    }

    @Test
    fun `enviar muestra la carga y luego el estado de exito`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.onEmailChange(" ana@correo.com ")

        viewModel.onSubmit()
        advanceTimeBy(ForgotPasswordViewModel.SEND_DELAY_MILLIS - 1)
        assertTrue(viewModel.uiState.value.isSending)
        assertFalse(viewModel.uiState.value.isSent)
        assertFalse(viewModel.uiState.value.canSubmit)

        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isSending)
        assertTrue(viewModel.uiState.value.isSent)
        assertEquals("ana@correo.com", viewModel.uiState.value.sentTo)
    }

    @Test
    fun `un correo invalido no se envia`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.onEmailChange("ana")

        viewModel.onSubmit()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSending)
        assertFalse(viewModel.uiState.value.isSent)
    }

    @Test
    fun `el correo no cambia mientras se envia ni despues`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.onEmailChange("ana@correo.com")

        viewModel.onSubmit()
        viewModel.onEmailChange("otro@correo.com")
        advanceUntilIdle()
        viewModel.onEmailChange("otro@correo.com")

        assertEquals("ana@correo.com", viewModel.uiState.value.email)
    }

    @Test
    fun `el estado de exito sobrevive a la muerte del proceso`() = runTest(dispatcher) {
        val savedStateHandle = SavedStateHandle()
        viewModel(savedStateHandle).apply {
            onEmailChange("ana@correo.com")
            onSubmit()
        }
        advanceUntilIdle()

        val restored = viewModel(savedStateHandle).uiState.value

        assertTrue(restored.isSent)
        assertEquals("ana@correo.com", restored.sentTo)
    }
}
