package com.segundoaire.presentation.onboarding

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.GlassCard
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

@Composable
private fun OnboardingEyebrowSample() {
    GlassCard {
        OnboardingEyebrow(stringResource(R.string.onboarding_reveal_formula_eyebrow))
    }
}

@Preview(name = "OnboardingEyebrow · claro")
@Composable
private fun OnboardingEyebrowLightPreview() {
    SegundoAireTheme(darkTheme = false) { OnboardingEyebrowSample() }
}

@Preview(name = "OnboardingEyebrow · oscuro")
@Composable
private fun OnboardingEyebrowDarkPreview() {
    SegundoAireTheme(darkTheme = true) { OnboardingEyebrowSample() }
}
