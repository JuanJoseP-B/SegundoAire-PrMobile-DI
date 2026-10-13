package com.segundoaire.presentation.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.usecase.ValidateEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Recuperar contraseña, simulado (SEGUNDOAIRE_SPEC.md §5): valida el correo,
 * espera un momento y pasa al estado de éxito. No se envía ni se guarda nada.
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val validateEmail: ValidateEmailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(restoreState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        if (_uiState.value.isSending || _uiState.value.isSent) return
        _uiState.update { it.copy(email = value, isEmailValid = validateEmail(value)) }
        savedStateHandle[KEY_EMAIL] = value
    }

    fun onEmailBlur() {
        _uiState.update { it.copy(emailTouched = true) }
    }

    fun onSubmit() {
        if (!_uiState.value.canSubmit) return

        _uiState.update { it.copy(isSending = true) }
        viewModelScope.launch {
            delay(SEND_DELAY_MILLIS)
            _uiState.update { it.copy(isSending = false, isSent = true) }
            savedStateHandle[KEY_SENT] = true
        }
    }

    private fun restoreState(): ForgotPasswordUiState {
        val email = savedStateHandle.get<String>(KEY_EMAIL).orEmpty()
        val isEmailValid = validateEmail(email)
        return ForgotPasswordUiState(
            email = email,
            isEmailValid = isEmailValid,
            isSent = isEmailValid && savedStateHandle.get<Boolean>(KEY_SENT) == true
        )
    }

    companion object {
        /** Lo que tarda el envío simulado del enlace. */
        const val SEND_DELAY_MILLIS = 1200L

        private const val KEY_EMAIL = "forgot_email"
        private const val KEY_SENT = "forgot_sent"
    }
}
