package com.segundoaire.presentation.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.presentation.designsystem.components.ChoiceChipGroup
import com.segundoaire.presentation.designsystem.components.QuestionScaffold
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/** Paso 2: hasta dos objetivos. El primero que se elige es el principal. */
@Composable
fun GoalsScreen(
    goals: List<Goal>,
    canContinue: Boolean,
    onGoalsChange: (List<Goal>) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val options = Goal.entries
    QuestionScaffold(
        primaryLabel = stringResource(R.string.onboarding_continue),
        onPrimary = onContinue,
        modifier = modifier,
        primaryEnabled = canContinue,
        title = stringResource(R.string.onboarding_goals_title),
        subtitle = stringResource(R.string.onboarding_goals_subtitle, UserProfile.MAX_GOALS),
        step = OnboardingStep.GOALS.progress,
        totalSteps = OnboardingStep.PROGRESS_STEPS,
        onBack = onBack
    ) {
        ChoiceChipGroup(
            options = options.map { stringResource(it.labelRes) },
            // El conjunto conserva el orden de elección de la lista
            selected = goals.mapTo(linkedSetOf()) { it.ordinal },
            onSelectionChange = { selected -> onGoalsChange(selected.map { options[it] }) },
            modifier = Modifier.fillMaxWidth(),
            maxSelected = UserProfile.MAX_GOALS
        )
    }
}

@Preview(name = "Objetivos · claro")
@Composable
private fun GoalsScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) {
        GoalsScreen(
            goals = listOf(Goal.SLEEP),
            canContinue = true,
            onGoalsChange = {},
            onContinue = {},
            onBack = {}
        )
    }
}

@Preview(name = "Objetivos · oscuro")
@Composable
private fun GoalsScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        GoalsScreen(
            goals = listOf(Goal.FOCUS, Goal.PRESENCE),
            canContinue = true,
            onGoalsChange = {},
            onContinue = {},
            onBack = {}
        )
    }
}
