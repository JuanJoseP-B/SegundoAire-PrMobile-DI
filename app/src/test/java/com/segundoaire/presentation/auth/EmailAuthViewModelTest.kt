package com.segundoaire.presentation.auth

import androidx.lifecycle.SavedStateHandle
import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.domain.usecase.PasswordError
import com.segundoaire.domain.usecase.SaveAccountUseCase
import com.segundoaire.domain.usecase.ValidateEmailUseCase
import com.segundoaire.domain.usecase.ValidatePasswordUseCase
import com.segundoaire.testing.FakeUserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmailAuthViewModelTest {

    private val profiles = FakeUserProfileRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) = EmailAuthViewModel(
        savedStateHandle = savedStateHandle,
        validateEmail = ValidateEmailUseCase(),
        validatePassword = ValidatePasswordUseCase(),
        saveAccount = SaveAccountUseCase(profiles)
    )

    private fun EmailAuthViewModel.fillLogin() {
        onEmailChange("ana@correo.com")
        onPasswordChange("segundo4ire")
    }

    // Login

    @Test
    fun `empieza en login, vacio, sin errores y sin poder enviar`() {
        val state = viewModel().uiState.value

        assertEquals(EmailAuthMode.LOGIN, state.mode)
        assertFalse(state.canSubmit)
        assertFalse(state.showEmailError)
        assertNull(state.passwordError)
        assertFalse(state.showConfirmationError)
    }

    @Test
    fun `con correo y contrasena validos se puede entrar`() {
        val viewModel = viewModel()

        viewModel.fillLogin()

        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `un correo mal escrito no deja enviar`() {
        val viewModel = viewModel()
        viewModel.fillLogin()

        viewModel.onEmailChange("ana@correo")

        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `una contrasena que no cumple las reglas no deja enviar`() {
        val viewModel = viewModel()
        viewModel.fillLogin()

        listOf("abc1", "abcdefgh", "12345678").forEach { password ->
            viewModel.onPasswordChange(password)

            assertFalse(password, viewModel.uiState.value.canSubmit)
        }
    }

    // Errores en línea

    @Test
    fun `el error del correo espera a que el usuario salga del campo`() {
        val viewModel = viewModel()

        viewModel.onEmailChange("ana@")
        assertFalse(viewModel.uiState.value.showEmailError)

        viewModel.onFieldBlur(EmailAuthField.EMAIL)
        assertTrue(viewModel.uiState.value.showEmailError)
    }

    @Test
    fun `corregir el correo quita el error`() {
        val viewModel = viewModel()
        viewModel.onEmailChange("ana@")
        viewModel.onFieldBlur(EmailAuthField.EMAIL)

        viewModel.onEmailChange("ana@correo.com")

        assertFalse(viewModel.uiState.value.showEmailError)
    }

    @Test
    fun `un campo vacio no muestra error aunque se haya tocado`() {
        val viewModel = viewModel()

        EmailAuthField.entries.forEach(viewModel::onFieldBlur)

        val state = viewModel.uiState.value
        assertFalse(state.showEmailError)
        assertNull(state.passwordError)
        assertFalse(state.showConfirmationError)
    }

    @Test
    fun `el error de la contrasena dice lo primero que falta`() {
        val viewModel = viewModel()
        viewModel.onFieldBlur(EmailAuthField.PASSWORD)

        viewModel.onPasswordChange("abc")
        assertEquals(PasswordError.TOO_SHORT, viewModel.uiState.value.passwordError)

        viewModel.onPasswordChange("12345678")
        assertEquals(PasswordError.MISSING_LETTER, viewModel.uiState.value.passwordError)

        viewModel.onPasswordChange("abcdefgh")
        assertEquals(PasswordError.MISSING_NUMBER, viewModel.uiState.value.passwordError)

        viewModel.onPasswordChange("abcdefg1")
        assertNull(viewModel.uiState.value.passwordError)
    }

    // Registro

    @Test
    fun `el registro exige que la confirmacion sea igual`() {
        val viewModel = viewModel()
        viewModel.onToggleMode()
        viewModel.fillLogin()
        assertFalse(viewModel.uiState.value.canSubmit)

        viewModel.onConfirmationChange("segundo4irE")
        assertFalse(viewModel.uiState.value.canSubmit)

        viewModel.onConfirmationChange("segundo4ire")
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `una confirmacion distinta marca error al salir del campo`() {
        val viewModel = viewModel()
        viewModel.onToggleMode()
        viewModel.fillLogin()

        viewModel.onConfirmationChange("otra")
        assertFalse(viewModel.uiState.value.showConfirmationError)

        viewModel.onFieldBlur(EmailAuthField.CONFIRMATION)
        assertTrue(viewModel.uiState.value.showConfirmationError)
    }

    @Test
    fun `cambiar la contrasena despues de confirmarla vuelve a bloquear el envio`() {
        val viewModel = viewModel()
        viewModel.onToggleMode()
        viewModel.fillLogin()
        viewModel.onConfirmationChange("segundo4ire")

        viewModel.onPasswordChange("segundo4ire2")

        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `cambiar de modo conserva el correo y limpia las contrasenas`() {
        val viewModel = viewModel()
        viewModel.fillLogin()
        EmailAuthField.entries.forEach(viewModel::onFieldBlur)

        viewModel.onToggleMode()

        val state = viewModel.uiState.value
        assertEquals(EmailAuthMode.REGISTER, state.mode)
        assertEquals("ana@correo.com", state.email)
        assertEquals("", state.password)
        assertEquals("", state.confirmation)
        assertEquals(setOf(EmailAuthField.EMAIL), state.touched)
        assertFalse(state.canSubmit)

        viewModel.onToggleMode()
        assertEquals(EmailAuthMode.LOGIN, viewModel.uiState.value.mode)
    }

    // Envío

    @Test
    fun `enviar guarda solo el correo normalizado y el modo`() {
        val viewModel = viewModel()
        viewModel.onEmailChange(" Ana@Correo.com ")
        viewModel.onPasswordChange("segundo4ire")

        viewModel.onSubmit()

        assertEquals(UserProfile(authMode = AuthMode.EMAIL, email = "ana@correo.com"), profiles.saved)
        assertTrue(viewModel.uiState.value.isCompleted)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `el registro tambien guarda la cuenta de correo`() {
        val viewModel = viewModel()
        viewModel.onToggleMode()
        viewModel.fillLogin()
        viewModel.onConfirmationChange("segundo4ire")

        viewModel.onSubmit()

        assertEquals(AuthMode.EMAIL, profiles.saved?.authMode)
        assertTrue(viewModel.uiState.value.isCompleted)
    }

    @Test
    fun `un formulario invalido no guarda nada`() {
        val viewModel = viewModel()
        viewModel.onEmailChange("ana@correo.com")

        viewModel.onSubmit()

        assertNull(profiles.saved)
        assertFalse(viewModel.uiState.value.isCompleted)
    }

    @Test
    fun `enviar dos veces guarda una sola vez`() {
        val viewModel = viewModel()
        viewModel.fillLogin()

        viewModel.onSubmit()
        viewModel.onSubmit()

        assertEquals(1, profiles.saveCount)
    }

    @Test
    fun `si el guardado falla se puede reintentar`() {
        val viewModel = viewModel()
        viewModel.fillLogin()
        profiles.failNextSave = true

        viewModel.onSubmit()
        assertFalse(viewModel.uiState.value.isCompleted)
        assertTrue(viewModel.uiState.value.canSubmit)

        viewModel.onSubmit()
        assertTrue(viewModel.uiState.value.isCompleted)
    }

    // Estado guardado

    @Test
    fun `el correo y el modo sobreviven a la muerte del proceso, la contrasena no`() {
        val savedStateHandle = SavedStateHandle()
        viewModel(savedStateHandle).apply {
            onToggleMode()
            fillLogin()
            onConfirmationChange("segundo4ire")
        }

        val restored = viewModel(savedStateHandle).uiState.value

        assertEquals(EmailAuthMode.REGISTER, restored.mode)
        assertEquals("ana@correo.com", restored.email)
        assertTrue(restored.isEmailValid)
        assertEquals("", restored.password)
        assertEquals("", restored.confirmation)
        assertFalse(restored.canSubmit)
        assertFalse(savedStateHandle.keys().any { savedStateHandle.get<Any>(it) == "segundo4ire" })
    }
}
