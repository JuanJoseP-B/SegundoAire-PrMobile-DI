package com.segundoaire.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.tree.TreeRules
import kotlin.math.min
import kotlin.math.roundToInt

private const val MinFit = 0.7f
private const val MaxFit = 1.4f

/**
 * Marco de diseño de 390 × 844 dp del prototipo, ajustado a la pantalla. En vez de escalar
 * con una capa gráfica (que descuadraría el blur y los toques), cambia la densidad de lo
 * que contiene: dentro del marco, 1 dp es 1 unidad del prototipo en cualquier teléfono.
 * Lo que sobra por arriba y por abajo es del mismo color de fondo.
 */
@Composable
internal fun OnboardingFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    BoxWithConstraints(modifier.fillMaxSize().background(SegundoAireTheme.colors.background)) {
        val base = LocalDensity.current
        val topInset = WindowInsets.statusBars.getTop(base)
        val bottomInset = WindowInsets.navigationBars.getBottom(base)
        val availableHeight = (constraints.maxHeight - topInset - bottomInset).coerceAtLeast(1)

        val fit = min(
            constraints.maxWidth / (TreeRules.FrameWidth * base.density),
            availableHeight / (TreeRules.FrameHeight * base.density)
        ).coerceIn(MinFit, MaxFit)
        val frameDensity = Density(base.density * fit, base.fontScale)

        val frameWidth = (TreeRules.FrameWidth * frameDensity.density).roundToInt()
        val frameHeight = (TreeRules.FrameHeight * frameDensity.density).roundToInt()
        val framePosition = IntOffset(
            x = (constraints.maxWidth - frameWidth) / 2,
            y = topInset + (availableHeight - frameHeight) / 2
        )

        CompositionLocalProvider(LocalDensity provides frameDensity) {
            Box(
                Modifier
                    .offset { framePosition }
                    .requiredSize(TreeRules.FrameWidth.dp, TreeRules.FrameHeight.dp),
                content = content
            )
        }
    }
}
