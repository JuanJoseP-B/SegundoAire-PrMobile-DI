package com.segundoaire.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

// Un cuadro fijo de la animación, en claro y oscuro lado a lado. Cada preview fija el
// progreso de su fase con los mismos valores que la coreografía alcanza en ese momento.
@Composable
private fun PhasePreview(
    formScale: Float,
    formAlpha: Float,
    solidAlpha: Float = 0f,
    titleProgress: Float = 0f,
    overlayAlpha: Float = 1f
) {
    Row {
        listOf(false, true).forEach { dark ->
            SegundoAireTheme(darkTheme = dark) {
                Box(
                    Modifier
                        .width(180.dp)
                        .height(360.dp)
                        .clipToBounds()
                        .graphicsLayer { alpha = overlayAlpha }
                        .background(SegundoAireTheme.colors.background)
                ) {
                    BreathSplashContent(
                        formScale = { formScale },
                        formAlpha = { formAlpha },
                        solidAlpha = { solidAlpha },
                        titleProgress = { titleProgress },
                        animated = false
                    )
                }
            }
        }
    }
}

@Preview(name = "Fase 1 · Presencia (60 %)", widthDp = 360, heightDp = 360)
@Composable
private fun PresencePreview() = PhasePreview(
    formScale = 0.8f,
    formAlpha = 0.6f
)

@Preview(name = "Fase 2 · Respiro (inhalación)", widthDp = 360, heightDp = 360)
@Composable
private fun BreathPreview() = PhasePreview(
    formScale = BreathSplashSpec.BreathScale,
    formAlpha = 1f
)

@Preview(name = "Fase 3 · Bocanada profunda", widthDp = 360, heightDp = 360)
@Composable
private fun DeepBreathPreview() = PhasePreview(
    formScale = BreathSplashSpec.DeepScale,
    formAlpha = 1f
)

@Preview(name = "Fase 4 · Liberación (mitad)", widthDp = 360, heightDp = 360)
@Composable
private fun ReleasePreview() = PhasePreview(
    formScale = 4f,
    formAlpha = 1f,
    solidAlpha = 0.5f
)

@Preview(name = "Fase 5 · Revelación", widthDp = 360, heightDp = 360)
@Composable
private fun RevealPreview() = PhasePreview(
    formScale = 8f,
    formAlpha = 1f,
    solidAlpha = 1f,
    titleProgress = 1f
)

@Preview(name = "Fase 6 · Ingreso (50 %)", widthDp = 360, heightDp = 360)
@Composable
private fun EntrancePreview() = PhasePreview(
    formScale = 8f,
    formAlpha = 1f,
    solidAlpha = 1f,
    titleProgress = 1f,
    overlayAlpha = 0.5f
)
