package com.segundoaire.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.emphasized

/**
 * Encabezado de Bloques y Progreso: eyebrow en mayúsculas y el título en Fraunces
 * 30 / 36 con la palabra marcada `*así*` en cursiva y `accent`.
 */
@Composable
fun ScreenHeader(eyebrow: String, title: String, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    Column(modifier.fillMaxWidth().semantics(mergeDescendants = true) { heading() }) {
        Text(
            text = eyebrow.toUpperCase(Locale.current),
            style = typography.labelSmCaps.copy(fontWeight = FontWeight.W600),
            color = colors.onBackgroundMuted
        )
        Spacer(Modifier.height(SegundoAireTheme.spacing.space1))
        Text(
            text = emphasized(title, typography.serifEmphasis.copy(color = colors.accent)),
            style = typography.serif(size = 30, lineHeight = 36, tracking = typography.serifTitle.letterSpacing),
            color = colors.onBackground
        )
    }
}

@Composable
private fun ScreenHeaderSample() {
    ScreenHeader(
        eyebrow = stringResource(R.string.nav_blocks),
        title = stringResource(R.string.blocks_title),
        modifier = Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(SegundoAireTheme.spacing.space5)
    )
}

@Preview(name = "ScreenHeader · claro")
@Composable
private fun ScreenHeaderLightPreview() {
    SegundoAireTheme(darkTheme = false) { ScreenHeaderSample() }
}

@Preview(name = "ScreenHeader · oscuro")
@Composable
private fun ScreenHeaderDarkPreview() {
    SegundoAireTheme(darkTheme = true) { ScreenHeaderSample() }
}
