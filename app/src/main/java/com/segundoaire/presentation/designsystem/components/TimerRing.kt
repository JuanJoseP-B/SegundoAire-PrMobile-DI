package com.segundoaire.presentation.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val TimerRingSize = 280.dp
private val TimerRingStroke = 10.dp

// El halo de la punta mide tres veces el grosor del anillo
private const val GlowHaloScale = 3f

/**
 * Anillo del bloque de enfoque. [progress] va de 0f a 1f. Mientras [running],
 * el anillo avanza de forma continua y lineal hasta completarse en
 * [remainingMillis] (`Motion.timer`), y cada cambio de [progress] lo vuelve a
 * sincronizar con el reloj real. Reducir movimiento no lo detiene: es tiempo real.
 */
@Composable
fun TimerRing(
    progress: Float,
    timeText: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    running: Boolean = false,
    remainingMillis: Int = 0,
    size: Dp = TimerRingSize,
    strokeWidth: Dp = TimerRingStroke
) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val target = progress.coerceIn(0f, 1f)
    val animated = remember { Animatable(target) }

    LaunchedEffect(target, running, remainingMillis) {
        animated.snapTo(target)
        if (running && remainingMillis > 0) {
            animated.animateTo(1f, motion.timer(remainingMillis))
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .progressSemantics(target)
            .drawBehind {
                val stroke = strokeWidth.toPx()
                // Deja sitio al halo para que no se salga del componente
                val inset = stroke * GlowHaloScale / 2
                val diameter = this.size.minDimension - inset * 2
                val topLeft = Offset((this.size.width - diameter) / 2, (this.size.height - diameter) / 2)
                val arcSize = Size(diameter, diameter)
                val sweep = 360f * animated.value

                drawArc(
                    color = colors.outlineVariant,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(stroke)
                )
                if (sweep > 0f) {
                    drawArc(
                        color = colors.primary,
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                    val angle = (sweep - 90f) * PI.toFloat() / 180f
                    val tip = Offset(
                        x = center.x + diameter / 2 * cos(angle),
                        y = center.y + diameter / 2 * sin(angle)
                    )
                    val haloRadius = stroke * GlowHaloScale / 2
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(colors.glow, colors.glow.copy(alpha = 0f)),
                            center = tip,
                            radius = haloRadius
                        ),
                        radius = haloRadius,
                        center = tip
                    )
                    drawCircle(color = colors.glow, radius = stroke / 2, center = tip)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeText,
                style = SegundoAireTheme.typography.displayTimer,
                color = colors.onBackground,
                maxLines = 1
            )
            if (label != null) {
                Text(
                    text = label,
                    style = SegundoAireTheme.typography.labelSmCaps,
                    color = colors.onBackgroundMuted
                )
            }
        }
    }
}

@Composable
private fun TimerRingSample() {
    val spacing = SegundoAireTheme.spacing
    Column(
        modifier = Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(spacing.space10),
        verticalArrangement = Arrangement.spacedBy(spacing.space10)
    ) {
        TimerRing(
            progress = 0.35f,
            timeText = stringResource(R.string.timer_ring_preview_time),
            label = stringResource(R.string.timer_state_focused)
        )
        TimerRing(
            progress = 0f,
            timeText = stringResource(R.string.timer_ring_preview_time),
            label = stringResource(R.string.timer_state_ready)
        )
    }
}

@Preview(name = "TimerRing · claro")
@Composable
private fun TimerRingLightPreview() {
    SegundoAireTheme(darkTheme = false) { TimerRingSample() }
}

@Preview(name = "TimerRing · oscuro")
@Composable
private fun TimerRingDarkPreview() {
    SegundoAireTheme(darkTheme = true) { TimerRingSample() }
}
