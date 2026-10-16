package com.segundoaire.presentation.onboarding.steps

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.OnboardingRules
import com.segundoaire.presentation.onboarding.StepButton
import com.segundoaire.presentation.onboarding.StepHeader
import kotlinx.coroutines.delay

private const val CanopySize = 260f
private const val CanopyMinScale = 0.62f
private val CanopyPivot = Offset(130f, 146.2f)
private const val HalfCycleMillis = 4000L

@Composable
internal fun BreathText(step: Int, total: Int) {
    StepHeader(
        eyebrow = stringResource(R.string.onb_step_of, step, total),
        title = stringResource(R.string.onb_breath_title),
        subtitle = stringResource(R.string.onb_breath_subtitle)
    )
}

/**
 * La copa respira de 0.62 a 1 en 8 s, tres ciclos, con "Inhala" y "Exhala". "Saltar" pasa a
 * "Continuar" a los 24 s ([done]). Con reducir movimiento la copa se queda quieta y solo
 * cambia la palabra.
 */
@Composable
internal fun BreathContent(active: Boolean, done: Boolean, onContinue: () -> Unit) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val motion = SegundoAireTheme.motion

    val canopy = remember { Animatable(if (motion.reduced) 1f else CanopyMinScale) }
    var inhale by remember { mutableStateOf(true) }

    LaunchedEffect(active) {
        inhale = true
        canopy.snapTo(if (motion.reduced) 1f else CanopyMinScale)
        if (!active) return@LaunchedEffect
        repeat(OnboardingRules.BreathCycles) {
            inhale = true
            if (motion.reduced) {
                delay(HalfCycleMillis)
                inhale = false
                delay(HalfCycleMillis)
            } else {
                canopy.animateTo(1f, motion.breathHalf())
                inhale = false
                canopy.animateTo(CanopyMinScale, motion.breathHalf())
            }
        }
        inhale = true
    }

    val inAlpha = animateFloatAsState(if (inhale) 1f else 0f, motion.fade(400), label = "breathIn")
    val outAlpha = animateFloatAsState(if (inhale) 0f else 1f, motion.fade(400), label = "breathOut")

    Canvas(
        Modifier
            .offset(x = 65.dp, y = 270.dp)
            .size(CanopySize.dp)
    ) {
        scale(density, Offset.Zero) {
            scale(canopy.value, CanopyPivot) {
                drawCircle(colors.glow.copy(alpha = 0.45f), 70f, Offset(130f, 120f))
                drawCircle(colors.primary.copy(alpha = 0.4f), 50f, Offset(80f, 140f))
                drawCircle(colors.mint.copy(alpha = 0.75f), 50f, Offset(180f, 140f))
                drawCircle(colors.mint.copy(alpha = 0.6f), 45f, Offset(105f, 90f))
                drawCircle(colors.primary.copy(alpha = 0.35f), 45f, Offset(160f, 88f))
                drawCircle(colors.glow.copy(alpha = 0.35f), 55f, Offset(130f, 160f))
            }
        }
    }
    Box(
        Modifier
            .offset(y = 542.dp)
            .fillMaxWidth()
            .height(40.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        contentAlignment = Alignment.TopCenter
    ) {
        val style = typography.serif(size = 26, lineHeight = 32, weight = androidx.compose.ui.text.font.FontWeight.W400, italic = true)
        Text(
            text = stringResource(R.string.onb_breath_in),
            modifier = Modifier.graphicsLayer { alpha = inAlpha.value },
            style = style,
            color = colors.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.onb_breath_out),
            modifier = Modifier.graphicsLayer { alpha = outAlpha.value },
            style = style,
            color = colors.primary,
            textAlign = TextAlign.Center
        )
    }
    StepButton(
        text = stringResource(if (done) R.string.onboarding_continue else R.string.onb_breath_skip),
        onClick = onContinue
    )
}
