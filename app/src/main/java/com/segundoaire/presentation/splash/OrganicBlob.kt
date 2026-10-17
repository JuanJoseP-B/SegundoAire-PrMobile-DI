package com.segundoaire.presentation.splash

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

// Opacidad del centro mint: deja ver el degradado sin que el blob parezca un hueco
private const val CenterAlpha = 0.6f

// Todo lo que se reutiliza en cada cuadro: el blob no crea objetos mientras respira
private class BlobBuffers {
    val xs = FloatArray(BlobGeometry.PointCount)
    val ys = FloatArray(BlobGeometry.PointCount)
    val segments = FloatArray(BlobGeometry.PointCount * BlobGeometry.FloatsPerSegment)
    val path = Path()
}

/**
 * Blob orgánico de El Respiro, centrado en el área. Relleno con un degradado radial
 * de `mint` (translúcido) al centro a `primary` en el borde.
 *
 * [scale] y [solid] son lambdas para leerse en la fase de dibujo: animarlos no
 * recompone. [scale] agranda el blob desde su centro; [solid] (0 a 1) pinta encima
 * un relleno `primary` plano, para que al cubrir la pantalla no quede mint detrás
 * del texto.
 */
@Composable
fun OrganicBlob(
    scale: () -> Float,
    modifier: Modifier = Modifier,
    solid: () -> Float = { 0f },
    staticPhase: Float? = null
) {
    val colors = SegundoAireTheme.colors
    val buffers = remember { BlobBuffers() }
    // Una sola transición para toda la oscilación del borde
    val animatedPhase = rememberInfiniteTransition(label = "blobPhase").animateFloat(
        initialValue = 0f,
        targetValue = BlobGeometry.TwoPi,
        animationSpec = SegundoAireTheme.motion.splashPhase(),
        label = "blobPhase"
    )

    Box(
        modifier.drawWithCache {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = BlobGeometry.baseRadius(size.width, size.height)
            val fill = Brush.radialGradient(
                0f to colors.mint.copy(alpha = CenterAlpha),
                1f to colors.primary,
                center = center,
                radius = baseRadius
            )
            onDrawBehind {
                val current = scale()
                val phase = staticPhase ?: animatedPhase.value
                BlobGeometry.points(center.x, center.y, baseRadius, phase, buffers.xs, buffers.ys)
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

                withTransform({ this.scale(current, current, center) }) {
                    drawPath(path, fill)
                    val flat = solid()
                    if (flat > 0f) drawPath(path, colors.primary, alpha = flat)
                }
            }
        }
    )
}

@Composable
private fun OrganicBlobSample(darkTheme: Boolean) {
    SegundoAireTheme(darkTheme = darkTheme) {
        OrganicBlob(
            scale = { 1f },
            staticPhase = 0.8f,
            modifier = Modifier
                .background(SegundoAireTheme.colors.background)
                .width(240.dp)
                .height(320.dp)
        )
    }
}

@Preview(name = "Blob · claro")
@Composable
private fun OrganicBlobLightPreview() = OrganicBlobSample(darkTheme = false)

@Preview(name = "Blob · oscuro")
@Composable
private fun OrganicBlobDarkPreview() = OrganicBlobSample(darkTheme = true)
