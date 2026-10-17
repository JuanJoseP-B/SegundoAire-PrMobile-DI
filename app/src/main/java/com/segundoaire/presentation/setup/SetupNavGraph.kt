package com.segundoaire.presentation.setup

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.segundoaire.presentation.designsystem.theme.SegundoAireMotion
import com.segundoaire.presentation.navigation.flowGraph

const val SetupGraphRoute = "setup"

private const val AppsDestination = "setup/apps"

/**
 * Grafo anidado del setup: la selección de apps. Los permisos ya se piden en el onboarding
 * (paso 11); si quedaron pendientes, el Panel muestra el banner de permisos. [onCompleted] se
 * llama una vez que la selección quedó guardada y el setup marcado como completo.
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
        startDestination = AppsDestination,
        motion = motion,
        stepOffsetPx = stepOffsetPx,
        forward = forward
    ) {
        composable(AppsDestination) {
            AppSelectionRoute(onCompleted = onCompleted)
        }
    }
}
