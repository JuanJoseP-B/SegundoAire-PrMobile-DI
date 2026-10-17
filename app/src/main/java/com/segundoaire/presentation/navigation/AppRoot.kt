package com.segundoaire.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.segundoaire.presentation.designsystem.components.LocalEntranceHold
import com.segundoaire.presentation.splash.BreathSplash

private const val SplashPending = 0
private const val SplashRunning = 1
private const val SplashDone = 2

/**
 * Raíz de la app: el [AppNavHost] y, encima, el splash El Respiro. El grafo ya está
 * compuesto debajo, así la pantalla de bienvenida (o el Panel) está lista cuando el
 * overlay se funde, y sus tarjetas entran escalonadas justo al empezar ese fundido.
 */
@Composable
fun AppRoot(viewModel: AppViewModel = hiltViewModel()) {
    val destination by viewModel.startDestination.collectAsState()

    // Se guarda con la instancia: una rotación no reinicia el splash ni lo repite al terminar
    var splash by rememberSaveable { mutableIntStateOf(SplashPending) }
    // Si la actividad se recrea con el splash a medias, se da por terminado en vez de empezar de nuevo
    val recreatedMidway = remember { splash == SplashRunning }
    val active = !recreatedMidway && splash != SplashDone
    // Falso desde que empieza la fase 6: las entradas de la pantalla de abajo ya pueden arrancar
    var exiting by remember { mutableStateOf(false) }
    val showSplash = active && destination != null

    LaunchedEffect(recreatedMidway, showSplash) {
        if (recreatedMidway) {
            splash = SplashDone
        } else if (showSplash && splash == SplashPending) {
            splash = SplashRunning
        }
    }

    CompositionLocalProvider(LocalEntranceHold provides (active && !exiting)) {
        Box(Modifier.fillMaxSize()) {
            AppNavHost(viewModel)
            if (showSplash) {
                BreathSplash(
                    onFinished = { splash = SplashDone },
                    onExitStarted = { exiting = true }
                )
            }
        }
    }
}
