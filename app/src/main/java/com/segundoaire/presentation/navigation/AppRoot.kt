package com.segundoaire.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.splash.SplashOverlay
import com.segundoaire.presentation.splash.SplashTimeline

private const val SplashPending = 0
private const val SplashRunning = 1
private const val SplashDone = 2

/**
 * Raíz de la app: el [AppNavHost] y, encima, el splash El Respiro. El grafo ya está
 * compuesto debajo, así el onboarding (o el Panel) aparece al fundirse el overlay,
 * sin pantalla intermedia.
 *
 * La versión completa se muestra si el onboarding no está completado (primera
 * ejecución o tras "Reiniciar app"); si ya lo está, la abreviada.
 */
@Composable
fun AppRoot(viewModel: AppViewModel = hiltViewModel()) {
    val destination by viewModel.startDestination.collectAsState()
    val reducedMotion = SegundoAireTheme.motion.reduced

    // Se guarda con la instancia: una rotación no reinicia el splash ni lo repite al terminar
    var splash by rememberSaveable { mutableIntStateOf(SplashPending) }
    // Si la actividad se recrea con el splash a medias, se da por terminado en vez de empezar de nuevo
    val recreatedMidway = remember { splash == SplashRunning }
    // La variante se fija al primer destino leído, para que un cambio posterior no reinicie la animación
    val variant = remember(destination != null) {
        destination?.let { SplashTimeline.variantFor(it, reducedMotion) }
    }

    LaunchedEffect(recreatedMidway, variant) {
        if (recreatedMidway) {
            splash = SplashDone
        } else if (variant != null && splash == SplashPending) {
            splash = SplashRunning
        }
    }

    Box(Modifier.fillMaxSize()) {
        AppNavHost(viewModel)
        if (variant != null && !recreatedMidway && splash != SplashDone) {
            SplashOverlay(variant = variant, onFinished = { splash = SplashDone })
        }
    }
}
