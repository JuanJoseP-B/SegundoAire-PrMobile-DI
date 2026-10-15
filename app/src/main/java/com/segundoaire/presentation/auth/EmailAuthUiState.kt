package com.segundoaire.presentation.auth

import com.segundoaire.domain.usecase.PasswordError

enum class EmailAuthMode { LOGIN, REGISTER }

enum class EmailAuthField { EMAIL, PASSWORD, CONFIRMATION }

/**
 * Estado del formulario de correo, que sirve para entrar y para registrarse.
 *
 * [isEmailValid] y [passwordIssue] reflejan siempre lo escrito y deciden si se
 * puede enviar. Los errores en línea son más pacientes: solo aparecen en los
 * campos de [touched] (los que el usuario ya dejó) y nunca en uno vacío, para
 * no corregir a alguien que apenas empieza a escribir.
 */
data class EmailAuthUiState(
    val mode: EmailAuthMode = EmailAuthMode.LOGIN,
    val email: String = "",
    val password: String = "",
    val confirmation: String = "",
    val isEmailValid: Boolean = false,
    val passwordIssue: PasswordError? = PasswordError.TOO_SHORT,
    val touched: Set<EmailAuthField> = emptySet(),
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false
) {
    val isRegister: Boolean get() = mode == EmailAuthMode.REGISTER

    val showEmailError: Boolean
        get() = EmailAuthField.EMAIL in touched && email.isNotBlank() && !isEmailValid

    val passwordError: PasswordError?
        get() = passwordIssue.takeIf { EmailAuthField.PASSWORD in touched && password.isNotEmpty() }

    val showConfirmationError: Boolean
        get() = isRegister && EmailAuthField.CONFIRMATION in touched &&
            confirmation.isNotEmpty() && confirmation != password

    val canSubmit: Boolean
        get() = isEmailValid && passwordIssue == null && (!isRegister || confirmation == password) &&
            !isSubmitting && !isCompleted
}
