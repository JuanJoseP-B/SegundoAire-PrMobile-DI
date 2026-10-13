package com.segundoaire.presentation.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.Motion
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/**
 * Texto de la IA que se revela carácter a carácter (`Motion.reveal`): 18 ms
 * entre caracteres, cada uno con fundido y una subida de 4 dp. El texto completo
 * ocupa su sitio desde el principio, así que el revelado no mueve el layout, y
 * los lectores de pantalla lo reciben entero (§10). Con reducir movimiento todo
 * el texto se funde a la vez. Se reinicia si [text] cambia.
 */
@Composable
fun RevealText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = SegundoAireTheme.typography.bodyLg,
    color: Color = SegundoAireTheme.colors.onBackground,
    onRevealed: () -> Unit = {}
) {
    val motion = SegundoAireTheme.motion
    val charDelay = motion.revealCharDelayMillis
    val totalMillis = Motion.RevealCharFadeMillis + charDelay * (text.length - 1).coerceAtLeast(0)
    val elapsed = remember(text) { Animatable(0f) }
    val currentOnRevealed by rememberUpdatedState(onRevealed)

    LaunchedEffect(text, charDelay) {
        elapsed.snapTo(0f)
        // Reloj lineal del revelado; cada carácter calcula su avance a partir de él
        elapsed.animateTo(totalMillis.toFloat(), motion.timer(totalMillis))
        currentOnRevealed()
    }

    val riseEm = with(LocalDensity.current) { motion.revealRise.toPx() / style.fontSize.toPx() }
    Text(
        text = revealedString(text, elapsed.value, charDelay, color, riseEm),
        modifier = modifier.clearAndSetSemantics { contentDescription = text },
        style = style
    )
}

/** Avance de 0f a 1f del carácter [index] cuando han pasado [elapsedMillis]. */
internal fun revealProgress(index: Int, elapsedMillis: Float, charDelayMillis: Int, fadeMillis: Int): Float =
    ((elapsedMillis - index * charDelayMillis) / fadeMillis).coerceIn(0f, 1f)

// Solo los caracteres en vuelo llevan un span propio: los ya revelados comparten
// uno y los que faltan otro, transparente.
private fun revealedString(
    text: String,
    elapsedMillis: Float,
    charDelayMillis: Int,
    color: Color,
    riseEm: Float
): AnnotatedString = buildAnnotatedString {
    append(text)
    var revealedEnd = 0
    for (index in text.indices) {
        val progress = revealProgress(index, elapsedMillis, charDelayMillis, Motion.RevealCharFadeMillis)
        if (progress >= 1f) {
            revealedEnd = index + 1
        } else if (progress <= 0f) {
            addStyle(SpanStyle(color = color.copy(alpha = 0f)), index, text.length)
            break
        } else {
            addStyle(
                SpanStyle(
                    color = color.copy(alpha = color.alpha * progress),
                    baselineShift = BaselineShift(-riseEm * (1f - progress))
                ),
                index,
                index + 1
            )
        }
    }
    addStyle(SpanStyle(color = color), 0, revealedEnd)
}

@Composable
private fun RevealTextSample() {
    Box(
        Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(SegundoAireTheme.spacing.space6)
    ) {
        RevealText(text = stringResource(R.string.onboarding_subtitle))
    }
}

@Preview(name = "RevealText · claro")
@Composable
private fun RevealTextLightPreview() {
    SegundoAireTheme(darkTheme = false) { RevealTextSample() }
}

@Preview(name = "RevealText · oscuro")
@Composable
private fun RevealTextDarkPreview() {
    SegundoAireTheme(darkTheme = true) { RevealTextSample() }
}
