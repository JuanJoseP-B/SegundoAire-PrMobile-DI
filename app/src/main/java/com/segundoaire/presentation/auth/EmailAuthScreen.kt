package com.segundoaire.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.domain.usecase.PasswordError
import com.segundoaire.domain.usecase.ValidatePasswordUseCase
import com.segundoaire.presentation.designsystem.components.GlassTextField
import com.segundoaire.presentation.designsystem.components.QuestionScaffold
import com.segundoaire.presentation.designsystem.components.TextButton
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/**
 * Login y registro con correo en una sola pantalla: el modo cambia el título,
 * suma la confirmación y la ayuda de la contraseña, y cambia el botón.
 */
@Composable
fun EmailAuthScreen(
    state: EmailAuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmationChange: (String) -> Unit,
    onFieldBlur: (EmailAuthField) -> Unit,
    onToggleMode: () -> Unit,
    onForgotPassword: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = SegundoAireTheme.spacing
    val editable = !state.isSubmitting && !state.isCompleted
    val submitFromKeyboard = KeyboardActions(onDone = { if (state.canSubmit) onSubmit() })

    QuestionScaffold(
        primaryLabel = stringResource(
            if (state.isRegister) R.string.auth_register_submit else R.string.auth_login_submit
        ),
        onPrimary = onSubmit,
        modifier = modifier,
        primaryEnabled = state.canSubmit,
        title = stringResource(
            if (state.isRegister) R.string.auth_register_title else R.string.auth_login_title
        ),
        subtitle = stringResource(
            if (state.isRegister) R.string.auth_register_subtitle else R.string.auth_login_subtitle
        ),
        onBack = onBack
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.space4)) {
            EmailField(
                value = state.email,
                onValueChange = onEmailChange,
                showError = state.showEmailError,
                onBlur = { onFieldBlur(EmailAuthField.EMAIL) },
                modifier = Modifier.fillMaxWidth(),
                enabled = editable
            )
            GlassTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = stringResource(R.string.auth_password_label),
                modifier = Modifier
                    .fillMaxWidth()
                    .onBlur { onFieldBlur(EmailAuthField.PASSWORD) },
                supportingText = if (state.isRegister) {
                    stringResource(R.string.auth_password_supporting, ValidatePasswordUseCase.MIN_LENGTH)
                } else {
                    null
                },
                errorMessage = when (state.passwordError) {
                    PasswordError.TOO_SHORT ->
                        stringResource(R.string.auth_password_error_short, ValidatePasswordUseCase.MIN_LENGTH)
                    PasswordError.MISSING_LETTER -> stringResource(R.string.auth_password_error_letter)
                    PasswordError.MISSING_NUMBER -> stringResource(R.string.auth_password_error_number)
                    null -> null
                },
                isPassword = true,
                enabled = editable,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (state.isRegister) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = submitFromKeyboard
            )
            if (state.isRegister) {
                GlassTextField(
                    value = state.confirmation,
                    onValueChange = onConfirmationChange,
                    label = stringResource(R.string.auth_confirmation_label),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onBlur { onFieldBlur(EmailAuthField.CONFIRMATION) },
                    errorMessage = if (state.showConfirmationError) {
                        stringResource(R.string.auth_confirmation_error)
                    } else {
                        null
                    },
                    isPassword = true,
                    enabled = editable,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = submitFromKeyboard
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!state.isRegister) {
                    TextButton(
                        text = stringResource(R.string.auth_login_forgot),
                        onClick = onForgotPassword,
                        enabled = editable
                    )
                }
                TextButton(
                    text = stringResource(
                        if (state.isRegister) R.string.auth_register_switch else R.string.auth_login_switch
                    ),
                    onClick = onToggleMode,
                    enabled = editable
                )
            }
        }
    }
}

@Composable
private fun EmailAuthScreenSample(state: EmailAuthUiState) {
    EmailAuthScreen(
        state = state,
        onEmailChange = {},
        onPasswordChange = {},
        onConfirmationChange = {},
        onFieldBlur = {},
        onToggleMode = {},
        onForgotPassword = {},
        onSubmit = {},
        onBack = {}
    )
}

@Preview(name = "Login con correo · claro")
@Composable
private fun EmailLoginLightPreview() {
    SegundoAireTheme(darkTheme = false) {
        EmailAuthScreenSample(
            EmailAuthUiState(
                email = stringResource(R.string.auth_email_placeholder),
                isEmailValid = true,
                password = stringResource(R.string.app_name),
                passwordIssue = null
            )
        )
    }
}

@Preview(name = "Registro con correo · oscuro")
@Composable
private fun EmailRegisterDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        EmailAuthScreenSample(
            EmailAuthUiState(
                mode = EmailAuthMode.REGISTER,
                email = stringResource(R.string.app_name),
                password = stringResource(R.string.app_name),
                passwordIssue = PasswordError.MISSING_NUMBER,
                confirmation = stringResource(R.string.timer_ring_preview_time),
                touched = EmailAuthField.entries.toSet()
            )
        )
    }
}
