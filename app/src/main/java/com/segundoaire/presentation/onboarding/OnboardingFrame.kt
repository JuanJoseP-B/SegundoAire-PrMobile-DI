package com.segundoaire.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.tree.TreeRules
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val MinFit = 0.7f
private const val MaxFit = 1.4f

/**
 * Dónde queda la columna de diseño de 390 × 844 dentro de la ventana, en píxeles. Las capas
 * de fondo que ocupan toda la pantalla (cielo, colina, telón) lo usan para dibujar las
 * coordenadas del prototipo sin quedarse en los límites de la columna.
 */
@Immutable
internal class FrameColumnOffset(val x: Float, val y: Float)

internal val LocalFrameColumn = compositionLocalOf { FrameColumnOffset(0f, 0f) }

/**
 * Marco del onboarding. Ocupa toda la ventana, también bajo las barras del sistema, así el
 * fondo llega a los cuatro bordes. Las capas de fondo se dibujan directamente sobre él; lo
 * que lleva coordenadas del prototipo (árbol, textos, botones) va dentro de [FrameColumn],
 * una columna de 390 × 844 dp centrada en la zona segura (barras del sistema más muesca).
 *
 * Para ajustarla a cada teléfono no se escala con una capa gráfica (descuadraría el blur y
 * los toques): se cambia la densidad de todo el marco, de modo que dentro de él 1 dp es 1
 * unidad del prototipo en cualquier teléfono.
 */
@Composable
internal fun OnboardingFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val safeArea = WindowInsets.systemBars.union(WindowInsets.displayCutout)

    BoxWithConstraints(modifier.fillMaxSize().background(SegundoAireTheme.colors.background)) {
        val base = LocalDensity.current
        val direction = LocalLayoutDirection.current
        val left = safeArea.getLeft(base, direction)
        val top = safeArea.getTop(base)
        val right = safeArea.getRight(base, direction)
        val bottom = safeArea.getBottom(base)
        val safeWidth = max(1, constraints.maxWidth - left - right)
        val safeHeight = max(1, constraints.maxHeight - top - bottom)

        val fit = min(
            safeWidth / (TreeRules.FrameWidth * base.density),
            safeHeight / (TreeRules.FrameHeight * base.density)
        ).coerceIn(MinFit, MaxFit)
        val frameDensity = Density(base.density * fit, base.fontScale)

        val columnWidth = (TreeRules.FrameWidth * frameDensity.density).roundToInt()
        val columnHeight = (TreeRules.FrameHeight * frameDensity.density).roundToInt()
        val column = remember(left, top, safeWidth, safeHeight, columnWidth, columnHeight) {
            FrameColumnOffset(
                x = (left + (safeWidth - columnWidth) / 2).toFloat(),
                y = (top + (safeHeight - columnHeight) / 2).toFloat()
            )
        }

        CompositionLocalProvider(
            LocalDensity provides frameDensity,
            LocalFrameColumn provides column
        ) {
            Box(Modifier.fillMaxSize(), content = content)
        }
    }
}

/** Contenido con coordenadas del prototipo: la columna de 390 × 844 dp del [OnboardingFrame]. */
@Composable
internal fun FrameColumn(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val column = LocalFrameColumn.current
    Box(
        modifier
            .offset { IntOffset(column.x.roundToInt(), column.y.roundToInt()) }
            .requiredSize(TreeRules.FrameWidth.dp, TreeRules.FrameHeight.dp),
        content = content
    )
}
