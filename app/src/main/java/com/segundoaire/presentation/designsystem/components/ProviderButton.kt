package com.segundoaire.presentation.designsystem.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/** Proveedores de la cuenta simulada. */
enum class AuthProvider(@StringRes internal val labelRes: Int) {
    Google(R.string.provider_continue_google),
    Apple(R.string.provider_continue_apple),
    Email(R.string.provider_continue_email)
}

/**
 * Botón "Continuar con…" de la pantalla de cuenta. Usa íconos genéricos (globo,
 * dispositivo, sobre): los logos de marca están prohibidos hasta producción
 * (DESIGN_SYSTEM.md §9), donde se cambian por los recursos oficiales.
 */
@Composable
fun ProviderButton(
    provider: AuthProvider,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null
) {
    SecondaryButton(
        text = stringResource(provider.labelRes),
        onClick = onClick,
        modifier = modifier,
        icon = provider.genericIcon,
        enabled = enabled,
        interactionSource = interactionSource
    )
}

private val AuthProvider.genericIcon: ImageVector
    get() = when (this) {
        AuthProvider.Google -> SegundoAireIcons.Globe
        AuthProvider.Apple -> SegundoAireIcons.Device
        AuthProvider.Email -> SegundoAireIcons.Envelope
    }

@Composable
private fun ProviderButtonSample() {
    val spacing = SegundoAireTheme.spacing
    Column(
        modifier = Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(spacing.space6),
        verticalArrangement = Arrangement.spacedBy(spacing.space3)
    ) {
        AuthProvider.entries.forEach { provider ->
            ProviderButton(provider = provider, onClick = {}, modifier = Modifier.fillMaxWidth())
        }
        ProviderButton(
            provider = AuthProvider.Email,
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )
    }
}

@Preview(name = "ProviderButton · claro")
@Composable
private fun ProviderButtonLightPreview() {
    SegundoAireTheme(darkTheme = false) { ProviderButtonSample() }
}

@Preview(name = "ProviderButton · oscuro")
@Composable
private fun ProviderButtonDarkPreview() {
    SegundoAireTheme(darkTheme = true) { ProviderButtonSample() }
}
