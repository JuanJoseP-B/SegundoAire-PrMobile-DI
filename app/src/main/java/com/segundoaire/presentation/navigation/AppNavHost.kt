package com.segundoaire.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.segundoaire.presentation.auth.AuthGraphRoute
import com.segundoaire.presentation.auth.authGraph
import com.segundoaire.presentation.designsystem.catalog.ComponentCatalogAvailable
import com.segundoaire.presentation.designsystem.catalog.ComponentCatalogRoute
import com.segundoaire.presentation.designsystem.catalog.componentCatalog
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.main.MainScreen
import com.segundoaire.presentation.onboarding.OnboardingGraphRoute
import com.segundoaire.presentation.onboarding.onboardingGraph

object Routes {
    const val HOME = "home"
}

@Composable
fun AppNavHost(viewModel: AppViewModel = hiltViewModel()) {
    // null = todavía leyendo DataStore; evita parpadeo de la pantalla incorrecta
    val completed by viewModel.isOnboardingCompleted.collectAsState()
    val onboardingDone = completed ?: run {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }

    val navController = rememberNavController()
    // Se fija al primer valor leído: al completarse el onboarding navega el propio
    // flujo, y cambiar el destino inicial en ese momento reconstruiría el grafo
    val startDestination = remember { if (onboardingDone) Routes.HOME else OnboardingGraphRoute }

    val motion = SegundoAireTheme.motion
    val stepOffsetPx = with(LocalDensity.current) { motion.stepOffset.roundToPx() }
    val forward = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1

    NavHost(
        navController = navController,
        startDestination = startDestination
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
                navController.navigate(Routes.HOME) {
                    popUpTo(AuthGraphRoute) { inclusive = true }
                }
            }
        )
        composable(Routes.HOME) {
            MainScreen(
                // Solo en debug: pulsación larga en el título del Panel
                onOpenComponentCatalog = if (ComponentCatalogAvailable) {
                    { navController.navigate(ComponentCatalogRoute) }
                } else {
                    null
                }
            )
        }
        componentCatalog()
    }
}
