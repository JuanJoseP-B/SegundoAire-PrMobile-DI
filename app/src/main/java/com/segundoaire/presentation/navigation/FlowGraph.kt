package com.segundoaire.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigation
import com.segundoaire.presentation.designsystem.components.stepEnterTransition
import com.segundoaire.presentation.designsystem.components.stepExitTransition
import com.segundoaire.presentation.designsystem.theme.SegundoAireMotion

/**
 * Grafo anidado de un flujo de primera ejecución (onboarding, cuenta, setup).
 *
 * Entre destinos del mismo grafo la transición es `Motion.step`. Al entrar o
 * salir del grafo, es un fundido con `Motion.nav`: cambiar de flujo no es dar un
 * paso dentro de él. [stepOffsetPx] es el token ya en píxeles y [forward] vale
 * 1, o -1 en idiomas de derecha a izquierda.
 */
fun NavGraphBuilder.flowGraph(
    route: String,
    startDestination: String,
    motion: SegundoAireMotion,
    stepOffsetPx: Int,
    forward: Int,
    builder: NavGraphBuilder.() -> Unit
) {
    navigation(
        route = route,
        startDestination = startDestination,
        enterTransition = {
            if (changesGraph()) fadeIn(motion.nav()) else stepEnterTransition(motion, stepOffsetPx, forward)
        },
        exitTransition = {
            if (changesGraph()) fadeOut(motion.nav()) else stepExitTransition(motion, stepOffsetPx, forward)
        },
        popEnterTransition = {
            if (changesGraph()) fadeIn(motion.nav()) else stepEnterTransition(motion, stepOffsetPx, -forward)
        },
        popExitTransition = {
            if (changesGraph()) fadeOut(motion.nav()) else stepExitTransition(motion, stepOffsetPx, -forward)
        },
        builder = builder
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.changesGraph(): Boolean =
    initialState.destination.parent?.route != targetState.destination.parent?.route

/** Solo navega si [current] es el destino al frente: un doble toque no apila la pantalla dos veces. */
fun NavHostController.navigateFrom(current: String, target: String) {
    if (currentDestination?.route == current) navigate(target)
}

fun NavHostController.popFrom(current: String) {
    if (currentDestination?.route == current) popBackStack()
}
