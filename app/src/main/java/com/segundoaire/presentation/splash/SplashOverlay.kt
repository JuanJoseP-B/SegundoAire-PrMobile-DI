package com.segundoaire.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/**
 * El Respiro: blob que respira, se expande y cubre la pantalla. Se dibuja encima de
 * la app, así lo que hay debajo ya está compuesto cuando el splash termina.
 */
@Composable
fun SplashOverlay(
    variant: SplashVariant,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val scale = remember { Animatable(1f) }
    var releaseFrom by remember { mutableFloatStateOf(1f) }
    val currentOnFinished by rememberUpdatedState(onFinished)

    BoxWithConstraints(modifier.fillMaxSize().background(colors.background)) {
        val target = releaseTargetScale(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())

        LaunchedEffect(variant, target) {
            playBlob(scale, variant, motion, target) { releaseFrom = it }
            currentOnFinished()
        }

        OrganicBlob(
            scale = { scale.value },
            // Relleno plano que sube con la liberación: al cubrir no queda mint detrás
            solid = { ((scale.value - releaseFrom) / (target - releaseFrom)).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Splash · claro", widthDp = 360, heightDp = 640)
@Composable
private fun SplashOverlayLightPreview() {
    SegundoAireTheme(darkTheme = false) { SplashOverlay(SplashVariant.Full, onFinished = {}) }
}

@Preview(name = "Splash · oscuro", widthDp = 360, heightDp = 640)
@Composable
private fun SplashOverlayDarkPreview() {
    SegundoAireTheme(darkTheme = true) { SplashOverlay(SplashVariant.Full, onFinished = {}) }
}
