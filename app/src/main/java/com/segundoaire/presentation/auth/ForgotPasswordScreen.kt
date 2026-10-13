package com.segundoaire.presentation.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.QuestionScaffold
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/**
 * Recuperar contraseña. Primero pide el correo; con `isSent` la misma pantalla
 * pasa al mensaje de éxito, cuyo botón regresa al login.
 */
@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onEmailBlur: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isSent) {
        QuestionScaffold(
            primaryLabel = stringResource(R.string.auth_forgot_sent_action),
            onPrimary = onBack,
            // El lector de pantalla anuncia el resultado al cambiar de estado
            modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
            title = stringResource(R.string.auth_forgot_sent_title),
            subtitle = stringResource(R.string.auth_forgot_sent_body, state.sentTo)
        ) {}
    } else {
        QuestionScaffold(
            primaryLabel = stringResource(
                if (state.isSending) R.string.auth_forgot_sending else R.string.auth_forgot_submit
            ),
            onPrimary = onSubmit,
            modifier = modifier,
            primaryEnabled = state.canSubmit,
            title = stringResource(R.string.auth_forgot_title),
            subtitle = stringResource(R.string.auth_forgot_subtitle),
            onBack = onBack
        ) {
            EmailField(
                value = state.email,
                onValueChange = onEmailChange,
                showError = state.showEmailError,
                onBlur = onEmailBlur,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSending,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { if (state.canSubmit) onSubmit() })
            )
        }
    }
}

@Preview(name = "Recuperar contraseña · claro")
@Composable
private fun ForgotPasswordLightPreview() {
    SegundoAireTheme(darkTheme = false) {
        ForgotPasswordScreen(
            state = ForgotPasswordUiState(
                email = stringResource(R.string.app_name),
                emailTouched = true
            ),
            onEmailChange = {},
            onEmailBlur = {},
            onSubmit = {},
            onBack = {}
        )
    }
}

@Preview(name = "Recuperar contraseña · enviado · oscuro")
@Composable
private fun ForgotPasswordSentDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        ForgotPasswordScreen(
            state = ForgotPasswordUiState(
                email = stringResource(R.string.auth_email_placeholder),
                isEmailValid = true,
                isSent = true
            ),
            onEmailChange = {},
            onEmailBlur = {},
            onSubmit = {},
            onBack = {}
        )
    }
}
