package com.segundoaire.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
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
 * Marco de diseño de 390 × 844 dp del prototipo, ajustado a la zona segura de la pantalla.
 * En vez de escalar con una capa gráfica (que descuadraría el blur y los toques), cambia la
 * densidad de lo que contiene: dentro del marco, 1 dp es 1 unidad del prototipo en cualquier
 * teléfono.
 *
 * La zona segura es la unión de las barras del sistema y la muesca (o el recorte de la
 * cámara), de modo que la hora, la batería y los íconos de notificaciones nunca queden
 * debajo de un botón o un texto, aunque la muesca sea más alta que la barra de estado.
 * Se aplica como padding de ventana: si un ancestro ya consumió parte de los insets, no se
 * descuentan dos veces. El fondo cubre toda la ventana, y lo que sobra alrededor del
 * marco es del mismo color; la colina y el cielo se extienden bajo las barras.
 */
@Composable
internal fun OnboardingFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val safeArea = WindowInsets.systemBars.union(WindowInsets.displayCutout)
    val safeSides = WindowInsetsSides.Top + WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal

    Box(modifier.fillMaxSize().background(SegundoAireTheme.colors.background)) {
        BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(safeArea.only(safeSides))) {
            val base = LocalDensity.current
            val fit = min(
                constraints.maxWidth / (TreeRules.FrameWidth * base.density),
                constraints.maxHeight / (TreeRules.FrameHeight * base.density)
            ).coerceIn(MinFit, MaxFit)
            val frameDensity = Density(base.density * fit, base.fontScale)

            val frameWidth = (TreeRules.FrameWidth * frameDensity.density).roundToInt()
            val frameHeight = (TreeRules.FrameHeight * frameDensity.density).roundToInt()
            val framePosition = IntOffset(
                x = (constraints.maxWidth - frameWidth) / 2,
                y = (constraints.maxHeight - frameHeight) / 2
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
}
