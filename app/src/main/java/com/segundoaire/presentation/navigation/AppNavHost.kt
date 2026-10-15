package com.segundoaire.presentation.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.segundoaire.domain.model.StartDestination
import com.segundoaire.presentation.auth.AuthGraphRoute
import com.segundoaire.presentation.auth.authGraph
import com.segundoaire.presentation.designsystem.catalog.ComponentCatalogAvailable
import com.segundoaire.presentation.designsystem.catalog.ComponentCatalogRoute
import com.segundoaire.presentation.designsystem.catalog.componentCatalog
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.main.MainShell
import com.segundoaire.presentation.onboarding.OnboardingGraphRoute
import com.segundoaire.presentation.onboarding.onboardingGraph
import com.segundoaire.presentation.setup.AppSelectionRoute
import com.segundoaire.presentation.setup.SetupGraphRoute
import com.segundoaire.presentation.setup.setupGraph
import com.segundoaire.presentation.tutorial.TutorialScreen

object Routes {
    const val HOME = "home"
    const val EDIT_APPS = "home/edit-apps"
    const val TUTORIAL = "home/tutorial"
}

private fun StartDestination.route(): String = when (this) {
    StartDestination.ONBOARDING -> OnboardingGraphRoute
    StartDestination.ACCOUNT -> AuthGraphRoute
    StartDestination.SETUP -> SetupGraphRoute
    StartDestination.MAIN -> Routes.HOME
}

/**
 * Navegación raíz: onboarding → cuenta → permisos → apps → principal.
 *
 * Se entra por el primer paso que falta (ver [StartDestination]), y cada flujo
 * al terminar se quita de la pila con `popUpTo` inclusivo: desde el Panel,
 * "atrás" cierra la app y nunca regresa a un flujo ya completado.
 */
@Composable
fun AppNavHost(viewModel: AppViewModel = hiltViewModel()) {
    // null = todavía leyendo DataStore; evita parpadeo de la pantalla incorrecta
    val destination by viewModel.startDestination.collectAsState()
    val resolved = destination ?: run {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }

    val navController = rememberNavController()
    // Se fija al primer valor leído: al completarse cada paso navega el propio
    // flujo, y cambiar el destino inicial en ese momento reconstruiría el grafo
    val startDestination = remember { resolved.route() }

    val motion = SegundoAireTheme.motion
    val stepOffsetPx = with(LocalDensity.current) { motion.stepOffset.roundToPx() }
    val forward = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1

    NavHost(
        navController = navController,
        startDestination = startDestination,
        // Al cambiar de flujo o de pantalla raíz: fundido con `Motion.nav`
        enterTransition = { fadeIn(motion.nav()) },
        exitTransition = { fadeOut(motion.nav()) },
        popEnterTransition = { fadeIn(motion.nav()) },
        popExitTransition = { fadeOut(motion.nav()) }
    ) {
        onboardingGraph(
            navController = navController,
            motion = motion,
            stepOffsetPx = stepOffsetPx,
            forward = forward,
            onCompleted = {
                navController.navigate(AuthGraphRoute) {
                    popUpTo(OnboardingGraphRoute) { inclusive = true }
                }
            }
        )
        authGraph(
            navController = navController,
            motion = motion,
            stepOffsetPx = stepOffsetPx,
            forward = forward,
            onCompleted = {
                navController.navigate(SetupGraphRoute) {
                    popUpTo(AuthGraphRoute) { inclusive = true }
                }
            }
        )
        setupGraph(
            navController = navController,
            motion = motion,
            stepOffsetPx = stepOffsetPx,
            forward = forward,
            onCompleted = {
                navController.navigate(Routes.HOME) {
                    popUpTo(SetupGraphRoute) { inclusive = true }
                }
            }
        )
        composable(Routes.HOME) {
            MainShell(
                onEditApps = { navController.navigate(Routes.EDIT_APPS) },
                onOpenTutorial = { navController.navigate(Routes.TUTORIAL) },
                // La app quedó como recién instalada: bienvenida y nada detrás, ni siquiera el Panel
                onResetCompleted = {
                    navController.navigate(OnboardingGraphRoute) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                // Solo en debug: pulsación larga en el título del Panel
                onOpenComponentCatalog = if (ComponentCatalogAvailable) {
                    { navController.navigate(ComponentCatalogRoute) }
                } else {
                    null
                }
            )
        }
        composable(Routes.EDIT_APPS) {
            // Edita la selección con las apps vigiladas marcadas; guardar o volver regresa al Panel
            AppSelectionRoute(
                onCompleted = { navController.popBackStack() },
                editing = true,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.TUTORIAL) {
            // Solo se abre desde Ajustes; terminar o volver regresa a ellos
            TutorialScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        componentCatalog()
    }
}
