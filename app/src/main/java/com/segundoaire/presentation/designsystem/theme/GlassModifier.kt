package com.segundoaire.presentation.designsystem.theme

import android.graphics.BlurMaskFilter
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.tooling.preview.Preview
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

/** Fondo que desenfocan los vidrios; `null` si la pantalla no declaró uno. */
val LocalGlassBackdrop = staticCompositionLocalOf<HazeState?> { null }

/**
 * `true` si la ventana ya desenfoca lo que tiene detrás: el GlassWall con
 * `FLAG_BLUR_BEHIND`, donde Haze no aplica porque el fondo es otra app.
 */
val LocalWindowBlurBehind = staticCompositionLocalOf { false }

/** Abre un fondo de vidrio para [content]: lo marca [glassBackdrop] y lo desenfoca [glass]. */
@Composable
fun GlassBackdrop(content: @Composable () -> Unit) {
    val state = remember { HazeState() }
    CompositionLocalProvider(LocalGlassBackdrop provides state, content = content)
}

/** Marca la capa de fondo de la pantalla. No debe contener a los vidrios que la desenfocan. */
@Composable
fun Modifier.glassBackdrop(): Modifier {
    val state = LocalGlassBackdrop.current ?: return this
    return hazeSource(state)
}

/**
 * Superficie de vidrio de DESIGN_SYSTEM.md §5: sombra, relleno, blur y borde de luz.
 * Desenfoca solo en Android 12+ y dentro de un [GlassBackdrop]. Si el blur lo pone
 * la ventana ([LocalWindowBlurBehind]) basta el relleno; en otro caso usa el relleno
 * con más opacidad. El contraste del texto nunca depende del blur.
 */
@Composable
fun Modifier.glass(
    level: GlassLevel,
    shape: Shape = SegundoAireTheme.shapes.radiusLg
): Modifier {
    val glass = SegundoAireTheme.glass
    val style = glass[level]
    val background = SegundoAireTheme.colors.background
    val backdrop = LocalGlassBackdrop.current
    val edge = remember(glass) { Brush.linearGradient(listOf(glass.edgeHi, glass.edgeLo)) }

    val surface = when {
        backdrop != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            Modifier.hazeEffect(backdrop) {
                blurEnabled = true
                blurRadius = style.blur
                backgroundColor = background
                tints = listOf(HazeTint(style.fill))
                noiseFactor = 0f
            }
        LocalWindowBlurBehind.current -> Modifier.background(style.fill)
        else -> Modifier.background(style.fillNoBlur)
    }

    return this
        .glassShadow(style.shadows, shape)
        .clip(shape)
        .then(surface)
        .border(glass.edgeWidth, edge, shape)
}

// La sombra se recorta fuera de la silueta para que no oscurezca el relleno translúcido.
private fun Modifier.glassShadow(shadows: List<GlassShadow>, shape: Shape): Modifier = drawWithCache {
    val path = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache)) }
    val paints = shadows.map { shadow ->
        Paint().apply {
            color = shadow.color
            asFrameworkPaint().maskFilter =
                BlurMaskFilter(shadow.blur.toPx(), BlurMaskFilter.Blur.NORMAL)
        }
    }
    val offsets = shadows.map { it.offsetY.toPx() }
    onDrawBehind {
        clipPath(path, ClipOp.Difference) {
            drawIntoCanvas { canvas ->
                paints.forEachIndexed { index, paint ->
                    canvas.save()
                    canvas.translate(0f, offsets[index])
                    canvas.drawPath(path, paint)
                    canvas.restore()
                }
            }
        }
    }
}

@Composable
private fun GlassLevelsSample() {
    val spacing = SegundoAireTheme.spacing
    Column(
        modifier = Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(spacing.space6),
        verticalArrangement = Arrangement.spacedBy(spacing.space6)
    ) {
        GlassLevel.entries.forEach { level ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(spacing.space16)
                    .glass(level)
            )
        }
    }
}

@Preview(name = "Vidrio · claro")
@Composable
private fun GlassLevelsLightPreview() {
    SegundoAireTheme(darkTheme = false) { GlassLevelsSample() }
}

@Preview(name = "Vidrio · oscuro")
@Composable
private fun GlassLevelsDarkPreview() {
    SegundoAireTheme(darkTheme = true) { GlassLevelsSample() }
}
