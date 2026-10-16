package com.segundoaire.presentation.designsystem.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/**
 * Dato de impacto que cuenta hasta [value] con `Motion.calm` cada vez que
 * cambia. Con reducir movimiento no cuenta: el número nuevo se funde. Los
 * lectores de pantalla solo reciben el valor final, nunca los intermedios.
 */
@Composable
fun AnimatedCounter(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = SegundoAireTheme.typography.displayImpact,
    color: Color = SegundoAireTheme.colors.onBackground,
    format: @Composable (Int) -> String = { it.toString() }
) {
    val motion = SegundoAireTheme.motion
    val description = format(value)
    val semantics = modifier.clearAndSetSemantics { contentDescription = description }

    if (motion.reduced) {
        Crossfade(
            targetState = value,
            modifier = semantics,
            animationSpec = motion.calm(),
            label = "counterFade"
        ) { shown ->
            Text(text = format(shown), style = style, color = color)
        }
    } else {
        val counted by animateIntAsState(
            targetValue = value,
            animationSpec = motion.calm(),
            label = "counter"
        )
        Text(text = format(counted), modifier = semantics, style = style, color = color)
    }
}

@Composable
private fun AnimatedCounterSample() {
    Column(
        modifier = Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(SegundoAireTheme.spacing.space6)
    ) {
        AnimatedCounter(value = 47) { stringResource(R.string.timer_duration_option, it) }
    }
}

@Preview(name = "AnimatedCounter · claro")
@Composable
private fun AnimatedCounterLightPreview() {
    SegundoAireTheme(darkTheme = false) { AnimatedCounterSample() }
}

@Preview(name = "AnimatedCounter · oscuro")
@Composable
private fun AnimatedCounterDarkPreview() {
    SegundoAireTheme(darkTheme = true) { AnimatedCounterSample() }
}
