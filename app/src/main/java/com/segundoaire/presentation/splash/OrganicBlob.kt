package com.segundoaire.presentation.splash

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.luminance
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

// Fases con las que se dibuja la forma cuando no se anima (reducir movimiento y previews)
private const val StaticPhaseA = 1.1f
private const val StaticPhaseB = 3.4f
private const val StaticRotation = 55f

// Todo lo que se reutiliza en cada cuadro: la forma no crea objetos mientras respira
private class BlobBuffers {
    val xs = FloatArray(BlobGeometry.PointCount)
    val ys = FloatArray(BlobGeometry.PointCount)
    val segments = FloatArray(BlobGeometry.PointCount * BlobGeometry.FloatsPerSegment)
    val path = Path()
}

private class FormPhases(
    val layerA: State<Float>,
    val layerB: State<Float>,
    val rotationDegrees: State<Float>
)

@Composable
private fun rememberFormPhases(animated: Boolean): FormPhases =
    if (animated) {
        // Una sola transición para toda la oscilación: las dos capas y la rotación
        val transition = rememberInfiniteTransition(label = "formPhases")
        FormPhases(
            layerA = transition.animateFloat(
                initialValue = 0f,
                targetValue = BlobGeometry.TwoPi,
                animationSpec = BreathSplashSpec.phase(BreathSplashSpec.LayerAPeriodMillis),
                label = "layerA"
            ),
            layerB = transition.animateFloat(
                initialValue = 0f,
                targetValue = BlobGeometry.TwoPi,
                animationSpec = BreathSplashSpec.phase(BreathSplashSpec.LayerBPeriodMillis),
                label = "layerB"
            ),
            rotationDegrees = transition.animateFloat(
                initialValue = BreathSplashSpec.RotationFromDegrees,
                targetValue = BreathSplashSpec.RotationToDegrees,
                animationSpec = BreathSplashSpec.rotation(),
                label = "rotation"
            )
        )
    } else {
        remember {
            FormPhases(
                layerA = mutableFloatStateOf(StaticPhaseA),
                layerB = mutableFloatStateOf(StaticPhaseB),
                rotationDegrees = mutableFloatStateOf(StaticRotation)
            )
        }
    }

/**
 * Forma orgánica de El Respiro, centrada en el área: dos capas translúcidas
 * superpuestas (`primary` y `glow`) más un halo radial de `glow`. El borde de cada
 * capa oscila con senos de fases distintas, a periodos de 7 s y 5.5 s, y la segunda
 * rota despacio entre 40° y 70°.
 *
 * [scale] y [alpha] son lambdas para leerse al dibujar: animarlos no recompone. Con
 * [animated] en `false` la forma queda quieta (reducir movimiento).
 *
 * En claro las capas se mezclan con Multiply; en oscuro, con Screen, porque
 * multiplicar colores sobre un fondo casi negro las haría desaparecer.
 */
@Composable
fun OrganicBlob(
    scale: () -> Float,
    alpha: () -> Float,
    modifier: Modifier = Modifier,
    animated: Boolean = true
) {
    val colors = SegundoAireTheme.colors
    val buffers = remember { BlobBuffers() }
    val phases = rememberFormPhases(animated)
    val blend = if (colors.background.luminance() < 0.5f) BlendMode.Screen else BlendMode.Multiply

    Box(
        modifier.drawWithCache {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = BreathSplashSpec.FormRadius.toPx()
            val haloRadius = radius * BreathSplashSpec.HaloRadiusFactor
            val halo = Brush.radialGradient(
                colors = listOf(
                    colors.glow.copy(alpha = BreathSplashSpec.HaloAlpha),
                    colors.glow.copy(alpha = 0f)
                ),
                center = center,
                radius = haloRadius
            )
            val layerA = colors.primary.copy(alpha = BreathSplashSpec.LayerAAlpha)
            val layerB = colors.glow.copy(alpha = BreathSplashSpec.LayerBAlpha)

            onDrawBehind {
                val current = scale()
                val visible = alpha()
                withTransform({ this.scale(current, current, center) }) {
                    drawCircle(halo, haloRadius, center, alpha = visible)
                    drawLayer(buffers, center, radius, phases.layerA.value, 0, layerA, visible, blend)
                    // La segunda capa rota sobre el mismo centro
                    withTransform({ rotate(phases.rotationDegrees.value, center) }) {
                        drawLayer(buffers, center, radius, phases.layerB.value, 1, layerB, visible, blend)
                    }
                }
            }
        }
    )
}

private fun DrawScope.drawLayer(
    buffers: BlobBuffers,
    center: Offset,
    radius: Float,
    phase: Float,
    layer: Int,
    color: Color,
    alpha: Float,
    blend: BlendMode
) {
    BlobGeometry.points(center.x, center.y, radius, phase, layer, buffers.xs, buffers.ys)
    BlobGeometry.bezierSegments(buffers.xs, buffers.ys, buffers.segments)

    val path = buffers.path
    path.rewind()
    path.moveTo(buffers.xs[0], buffers.ys[0])
    for (i in 0 until BlobGeometry.PointCount) {
        val o = i * BlobGeometry.FloatsPerSegment
        val s = buffers.segments
        path.cubicTo(s[o], s[o + 1], s[o + 2], s[o + 3], s[o + 4], s[o + 5])
    }
    path.close()
    drawPath(path, color, alpha = alpha, blendMode = blend)
}
