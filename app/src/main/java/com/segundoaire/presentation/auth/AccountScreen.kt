package com.segundoaire.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.domain.model.AuthMode
import com.segundoaire.presentation.designsystem.components.AuthProvider
import com.segundoaire.presentation.designsystem.components.ProviderButton
import com.segundoaire.presentation.designsystem.components.QuestionScaffold
import com.segundoaire.presentation.designsystem.components.TextButton
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/**
 * Paso 10 de la primera ejecución: elegir cómo continuar. Mientras un proveedor
 * "conecta", su botón sigue activo, los demás se apagan y se anuncia el estado.
 */
@Composable
fun AccountScreen(
    state: AccountUiState,
    onAuthModeSelected: (AuthMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val motion = SegundoAireTheme.motion

    QuestionScaffold(
        action = {
            TextButton(
                text = stringResource(R.string.auth_account_guest),
                onClick = { onAuthModeSelected(AuthMode.GUEST) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isBusy && !state.isCompleted
            )
        },
        modifier = modifier,
        title = stringResource(R.string.auth_account_title),
        subtitle = stringResource(R.string.auth_account_subtitle)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
            AuthProvider.entries.forEach { provider ->
                val mode = provider.authMode
                ProviderButton(
                    provider = provider,
                    onClick = { onAuthModeSelected(mode) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isCompleted && (!state.isBusy || state.loadingMode == mode)
                )
            }
        }
        AnimatedVisibility(
            visible = state.isConnecting,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enter = fadeIn(motion.nav()),
            exit = fadeOut(motion.nav())
        ) {
            Text(
                text = stringResource(R.string.auth_account_connecting),
                style = SegundoAireTheme.typography.bodyMd,
                color = colors.onBackgroundMuted,
                modifier = Modifier
                    .padding(top = spacing.space4)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
    }
}

private val AuthProvider.authMode: AuthMode
    get() = when (this) {
        AuthProvider.Google -> AuthMode.GOOGLE
        AuthProvider.Apple -> AuthMode.APPLE
        AuthProvider.Email -> AuthMode.EMAIL
    }

@Preview(name = "Cuenta · claro")
@Composable
private fun AccountScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) {
        AccountScreen(state = AccountUiState(), onAuthModeSelected = {})
    }
}

@Preview(name = "Cuenta · oscuro")
@Composable
private fun AccountScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        AccountScreen(state = AccountUiState(loadingMode = AuthMode.GOOGLE), onAuthModeSelected = {})
    }
}
