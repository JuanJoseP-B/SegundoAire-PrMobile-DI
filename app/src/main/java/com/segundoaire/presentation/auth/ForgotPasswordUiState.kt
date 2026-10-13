package com.segundoaire.presentation.auth

/**
 * Estado de recuperar contraseña. Como en el formulario de correo, el error
 * solo aparece cuando el usuario ya dejó el campo ([emailTouched]) y no está
 * vacío. Con [isSent] la pantalla pasa al mensaje de éxito.
 */
data class ForgotPasswordUiState(
    val email: String = "",
    val isEmailValid: Boolean = false,
    val emailTouched: Boolean = false,
    val isSending: Boolean = false,
    val isSent: Boolean = false
) {
    val showEmailError: Boolean get() = emailTouched && email.isNotBlank() && !isEmailValid

    val canSubmit: Boolean get() = isEmailValid && !isSending && !isSent

    /** El correo tal como se muestra en el mensaje de éxito. */
    val sentTo: String get() = email.trim()
}
