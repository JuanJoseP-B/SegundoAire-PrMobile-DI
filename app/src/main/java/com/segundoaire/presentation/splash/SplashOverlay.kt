package com.segundoaire.presentation.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import kotlinx.coroutines.delay

/**
 * El Respiro: blob que respira, se expande y cubre la pantalla; después aparece el
 * nombre de la app y todo el overlay se funde. Se dibuja encima de la app, así lo
 * que hay debajo ya está compuesto cuando el splash termina.
 *
 * Llama a [onFinished] una sola vez, cuando el overlay ya es transparente.
 */
@Composable
fun SplashOverlay(
    variant: SplashVariant,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val appName = stringResource(R.string.app_name)
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }
    var releaseFrom by remember { mutableFloatStateOf(1f) }
    var textVisible by remember { mutableStateOf(false) }
    val currentOnFinished by rememberUpdatedState(onFinished)
    // La subida del texto es 0 con reducir movimiento: queda solo el fundido
    val riseOffsetPx = with(LocalDensity.current) { motion.stepOffset.roundToPx() }

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            // El fundido de salida afecta a todo el overlay y se lee al dibujar
            .graphicsLayer { this.alpha = alpha.value }
            .background(colors.background)
    ) {
        val target = releaseTargetScale(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())

        LaunchedEffect(variant, target) {
            playBlob(scale, variant, motion, target) { releaseFrom = it }

            textVisible = true
            delay(motion.splashTextMillis.toLong())
            // La versión abreviada no se detiene: el texto y la salida van seguidos
            if (variant != SplashVariant.Short) delay(motion.splashHoldMillis.toLong())

            alpha.animateTo(0f, motion.splashExit())
            currentOnFinished()
        }

        OrganicBlob(
            scale = { scale.value },
            // Relleno plano que sube con la liberación: al cubrir no queda mint detrás
            solid = { ((scale.value - releaseFrom) / (target - releaseFrom)).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = textVisible,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(motion.splashText()) + slideInVertically(motion.calm()) { riseOffsetPx }
        ) {
            Text(
                text = appName,
                style = SegundoAireTheme.typography.headlineLg,
                color = colors.onPrimary
            )
        }
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
