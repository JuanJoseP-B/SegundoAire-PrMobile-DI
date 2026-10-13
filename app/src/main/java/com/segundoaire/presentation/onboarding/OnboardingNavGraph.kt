package com.segundoaire.presentation.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.segundoaire.presentation.designsystem.components.stepEnterTransition
import com.segundoaire.presentation.designsystem.components.stepExitTransition
import com.segundoaire.presentation.designsystem.theme.SegundoAireMotion

const val OnboardingGraphRoute = "onboarding"

/**
 * Grafo anidado del onboarding: un destino por paso, todos con el mismo
 * [OnboardingViewModel] (vive mientras el grafo esté en la pila) y la transición
 * `Motion.step` entre ellos. El botón de volver del sistema regresa un paso.
 *
 * [stepOffsetPx] es el desplazamiento del token ya en píxeles y [forward] vale 1,
 * o -1 en idiomas de derecha a izquierda. [onCompleted] se llama una vez que el
 * perfil quedó guardado.
 */
fun NavGraphBuilder.onboardingGraph(
    navController: NavHostController,
    motion: SegundoAireMotion,
    stepOffsetPx: Int,
    forward: Int,
    onCompleted: () -> Unit
) {
    navigation(
        route = OnboardingGraphRoute,
        startDestination = OnboardingStep.WELCOME.route,
        enterTransition = { stepEnterTransition(motion, stepOffsetPx, forward) },
        exitTransition = { stepExitTransition(motion, stepOffsetPx, forward) },
        popEnterTransition = { stepEnterTransition(motion, stepOffsetPx, -forward) },
        popExitTransition = { stepExitTransition(motion, stepOffsetPx, -forward) }
    ) {
        step(OnboardingStep.WELCOME, navController) { viewModel, state, goNext, _ ->
            WelcomeScreen(
                page = state.welcomePage,
                onNextPage = viewModel::onWelcomeNext,
                onPreviousPage = viewModel::onWelcomeBack,
                onContinue = goNext
            )
        }
        step(OnboardingStep.NAME, navController) { viewModel, state, goNext, goBack ->
            NameScreen(
                name = state.name,
                nameError = state.nameError,
                canContinue = state.canContinue(OnboardingStep.NAME),
                onNameChange = viewModel::onNameChange,
                onContinue = goNext,
                onBack = goBack
            )
        }
        step(OnboardingStep.GOALS, navController) { viewModel, state, goNext, goBack ->
            GoalsScreen(
                goals = state.goals,
                canContinue = state.canContinue(OnboardingStep.GOALS),
                onGoalsChange = viewModel::onGoalsChange,
                onContinue = goNext,
                onBack = goBack
            )
        }
        step(OnboardingStep.USAGE, navController) { viewModel, state, goNext, goBack ->
            UsageScreen(
                usageRange = state.usageRange,
                onUsageRangeChange = viewModel::onUsageRangeChange,
                onContinue = goNext,
                onBack = goBack
            )
        }
        step(OnboardingStep.MOMENTS, navController) { viewModel, state, goNext, goBack ->
            MomentsScreen(
                riskMoments = state.riskMoments,
                canContinue = state.canContinue(OnboardingStep.MOMENTS),
                onRiskMomentsChange = viewModel::onRiskMomentsChange,
                onContinue = goNext,
                onBack = goBack
            )
        }
        step(OnboardingStep.REVEAL, navController) { _, state, goNext, goBack ->
            RevealScreen(
                name = state.displayName,
                projection = state.projection,
                onContinue = goNext,
                onBack = goBack
            )
        }
        step(OnboardingStep.BREATHING, navController) { viewModel, state, goNext, goBack ->
            BreathingScreen(
                cyclesDone = state.breathCyclesDone,
                onCycleCompleted = viewModel::onBreathCycleCompleted,
                onContinue = goNext,
                onBack = goBack
            )
        }
        step(OnboardingStep.PLAN, navController) { _, state, goNext, goBack ->
            PlanScreen(
                name = state.displayName,
                goals = state.goals,
                suggestedRigor = state.suggestedRigor,
                riskMoments = state.riskMoments,
                onContinue = goNext,
                onBack = goBack
            )
        }
        step(OnboardingStep.COMMITMENT, navController) { viewModel, state, _, goBack ->
            val currentOnCompleted by rememberUpdatedState(onCompleted)
            LaunchedEffect(state.isCompleted) {
                if (state.isCompleted) currentOnCompleted()
            }
            CommitmentScreen(
                name = state.displayName,
                goals = state.goals,
                canCommit = state.canContinue(OnboardingStep.COMMITMENT) && !state.isCompleted,
                onCommit = viewModel::onCommit,
                onBack = goBack
            )
        }
    }
}

/**
 * Destino de [step]. Entrega a [content] el ViewModel del grafo, su estado y
 * las dos navegaciones del paso: al siguiente y al anterior.
 */
private fun NavGraphBuilder.step(
    step: OnboardingStep,
    navController: NavHostController,
    content: @Composable (
        viewModel: OnboardingViewModel,
        state: OnboardingUiState,
        goNext: () -> Unit,
        goBack: () -> Unit
    ) -> Unit
) {
    composable(step.route) { entry ->
        val graphEntry = remember(entry) { navController.getBackStackEntry(OnboardingGraphRoute) }
        val viewModel: OnboardingViewModel = hiltViewModel(graphEntry)
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        // Solo navega el destino que está al frente: un doble toque no apila el paso dos veces
        val isCurrent = { navController.currentDestination?.route == step.route }
        content(
            viewModel,
            state,
            { step.next?.takeIf { isCurrent() }?.let { navController.navigate(it.route) } },
            { if (isCurrent()) navController.popBackStack() }
        )
    }
}
