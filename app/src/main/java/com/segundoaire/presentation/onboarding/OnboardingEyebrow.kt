package com.segundoaire.presentation.onboarding

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/**
 * Eyebrow de una tarjeta del onboarding: `labelSm` en mayúsculas completas
 * (DESIGN_SYSTEM.md §2). Va en `onBackground` porque se usa sobre vidrio.
 */
@Composable
internal fun OnboardingEyebrow(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.toUpperCase(Locale.current),
        style = SegundoAireTheme.typography.labelSmCaps,
        color = SegundoAireTheme.colors.onBackground,
        modifier = modifier
    )
}
