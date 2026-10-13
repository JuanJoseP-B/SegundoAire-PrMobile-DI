package com.segundoaire.presentation.designsystem.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.Motion
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import kotlinx.coroutines.delay

private val BreathingCircleSize = 200.dp

// Tamaño del círculo al exhalar, respecto al de inhalar
private const val ExhaledScale = 0.6f

// El núcleo ocupa la mitad del diámetro; el resto es halo
private const val CoreScale = 0.5f

/**
 * Círculo de respiración: crece 4 s al inhalar y se encoge 4 s al exhalar
 * (`Motion.breath`). Con reducir movimiento se queda quieto y solo cambia la
 * indicación de texto, al mismo ritmo. [showLabel] muestra "Inhala" / "Exhala".
 */
@Composable
fun BreathingCircle(
    modifier: Modifier = Modifier,
    size: Dp = BreathingCircleSize,
    showLabel: Boolean = true
) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val scale = remember { Animatable(ExhaledScale) }
    var inhaling by remember { mutableStateOf(true) }

    LaunchedEffect(motion.reduced) {
        if (motion.reduced) {
            scale.snapTo(1f)
            while (true) {
                inhaling = true
                delay(Motion.BreathInhaleMillis.toLong())
                inhaling = false
                delay(Motion.BreathExhaleMillis.toLong())
            }
        } else {
            // Cada mitad del ciclo usa la curva del token
            val half = motion.breath().animation
            scale.snapTo(ExhaledScale)
            while (true) {
                inhaling = true
                scale.animateTo(1f, half)
                inhaling = false
                scale.animateTo(ExhaledScale, half)
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SegundoAireTheme.spacing.space4),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(size)
                .drawBehind {
                    val radius = this.size.minDimension / 2 * scale.value
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(colors.glow, colors.glow.copy(alpha = 0f)),
                            center = center,
                            radius = radius
                        ),
                        radius = radius
                    )
                    drawCircle(color = colors.primaryAlt, radius = radius * CoreScale)
                }
        )
        if (showLabel) {
            Crossfade(
                targetState = inhaling,
                animationSpec = motion.calm(),
                label = "breathLabel"
            ) { isInhaling ->
                Text(
                    text = stringResource(if (isInhaling) R.string.breath_inhale else R.string.breath_exhale),
                    style = SegundoAireTheme.typography.titleMd,
                    color = colors.onBackground
                )
            }
        }
    }
}

@Composable
private fun BreathingCircleSample() {
    Box(
        Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(SegundoAireTheme.spacing.space10)
    ) {
        BreathingCircle()
    }
}

@Preview(name = "BreathingCircle · claro")
@Composable
private fun BreathingCircleLightPreview() {
    SegundoAireTheme(darkTheme = false) { BreathingCircleSample() }
}

@Preview(name = "BreathingCircle · oscuro")
@Composable
private fun BreathingCircleDarkPreview() {
    SegundoAireTheme(darkTheme = true) { BreathingCircleSample() }
}
