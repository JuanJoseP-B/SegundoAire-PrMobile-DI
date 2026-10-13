package com.segundoaire.presentation.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.usecase.SaveAccountUseCase
import com.segundoaire.domain.usecase.ValidateEmailUseCase
import com.segundoaire.domain.usecase.ValidatePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

/**
 * Login y registro con correo. La validación es real, pero no hay backend
 * (SEGUNDOAIRE_SPEC.md §5): al enviar solo se guardan el correo y el modo de
 * acceso. La contraseña no sale de este ViewModel ni se copia al
 * [SavedStateHandle]; tras la muerte del proceso hay que volver a escribirla.
 */
@HiltViewModel
class EmailAuthViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val validateEmail: ValidateEmailUseCase,
    private val validatePassword: ValidatePasswordUseCase,
    private val saveAccount: SaveAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(restoreState())
    val uiState: StateFlow<EmailAuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, isEmailValid = validateEmail(value)) }
        savedStateHandle[KEY_EMAIL] = value
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordIssue = validatePassword(value)) }
    }

    fun onConfirmationChange(value: String) {
        _uiState.update { it.copy(confirmation = value) }
    }

    /** El usuario salió de [field]: desde ahora ese campo puede mostrar su error. */
    fun onFieldBlur(field: EmailAuthField) {
        _uiState.update { it.copy(touched = it.touched + field) }
    }

    /** Cambia entre entrar y registrarse. Conserva el correo y limpia las contraseñas. */
    fun onToggleMode() {
        if (_uiState.value.isSubmitting) return
        _uiState.update {
            it.copy(
                mode = if (it.isRegister) EmailAuthMode.LOGIN else EmailAuthMode.REGISTER,
                password = "",
                confirmation = "",
                passwordIssue = validatePassword(""),
                touched = it.touched - EmailAuthField.PASSWORD - EmailAuthField.CONFIRMATION
            )
        }
        savedStateHandle[KEY_MODE] = _uiState.value.mode.name
    }

    /** Guarda la cuenta y cierra el formulario, que navega al ver `isCompleted`. */
    fun onSubmit() {
        val state = _uiState.value
        if (!state.canSubmit) return

        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val saved = try {
                saveAccount(AuthMode.EMAIL, state.email)
                true
            } catch (_: IOException) {
                // DataStore no pudo escribir: el botón vuelve a quedar disponible para reintentar
                false
            }
            _uiState.update { it.copy(isSubmitting = false, isCompleted = saved) }
        }
    }

    private fun restoreState(): EmailAuthUiState {
        val email = savedStateHandle.get<String>(KEY_EMAIL).orEmpty()
        return EmailAuthUiState(
            mode = savedStateHandle.get<String>(KEY_MODE)
                ?.let { saved -> EmailAuthMode.entries.firstOrNull { it.name == saved } }
                ?: EmailAuthMode.LOGIN,
            email = email,
            isEmailValid = validateEmail(email)
        )
    }

    private companion object {
        const val KEY_EMAIL = "auth_email"
        const val KEY_MODE = "auth_mode"
    }
}
