package com.segundoaire.presentation.setup

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.segundoaire.presentation.designsystem.theme.SegundoAireMotion
import com.segundoaire.presentation.navigation.flowGraph
import com.segundoaire.presentation.navigation.navigateFrom

const val SetupGraphRoute = "setup"

private const val PermissionsDestination = "setup/permissions"
private const val AppsDestination = "setup/apps"

/**
 * Grafo anidado del setup: permisos → apps. [onCompleted] se llama una vez que
 * la selección quedó guardada y el setup marcado como completo.
 *
 * "Continuar" y "Hacerlo después" avanzan igual a las apps: lo que cambia es
 * si el Panel muestra después el banner de permisos pendientes.
 */
fun NavGraphBuilder.setupGraph(
    navController: NavHostController,
    motion: SegundoAireMotion,
    stepOffsetPx: Int,
    forward: Int,
    onCompleted: () -> Unit
) {
    flowGraph(
        route = SetupGraphRoute,
        startDestination = PermissionsDestination,
        motion = motion,
        stepOffsetPx = stepOffsetPx,
        forward = forward
    ) {
        composable(PermissionsDestination) {
            val goToApps = { navController.navigateFrom(PermissionsDestination, AppsDestination) }
            PermissionsRoute(onContinue = goToApps, onSkip = goToApps)
        }
        composable(AppsDestination) {
            AppSelectionRoute(onCompleted = onCompleted)
        }
    }
}
