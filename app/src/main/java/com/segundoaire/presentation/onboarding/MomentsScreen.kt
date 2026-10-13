package com.segundoaire.presentation.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.domain.model.RiskMoment
import com.segundoaire.presentation.designsystem.components.ChoiceChipGroup
import com.segundoaire.presentation.designsystem.components.QuestionScaffold
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/** Paso 4: momentos del día de mayor uso. Se pueden elegir varios. */
@Composable
fun MomentsScreen(
    riskMoments: Set<RiskMoment>,
    canContinue: Boolean,
    onRiskMomentsChange: (Set<RiskMoment>) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val options = RiskMoment.entries
    QuestionScaffold(
        primaryLabel = stringResource(R.string.onboarding_continue),
        onPrimary = onContinue,
        modifier = modifier,
        primaryEnabled = canContinue,
        title = stringResource(R.string.onboarding_moments_title),
        subtitle = stringResource(R.string.onboarding_moments_subtitle),
        step = OnboardingStep.MOMENTS.progress,
        totalSteps = OnboardingStep.PROGRESS_STEPS,
        onBack = onBack
    ) {
        ChoiceChipGroup(
            options = options.map { stringResource(it.labelRes) },
            selected = riskMoments.mapTo(mutableSetOf()) { it.ordinal },
            onSelectionChange = { selected ->
                onRiskMomentsChange(selected.mapTo(mutableSetOf()) { options[it] })
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Momentos · claro")
@Composable
private fun MomentsScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) {
        MomentsScreen(
            riskMoments = setOf(RiskMoment.BEFORE_SLEEP),
            canContinue = true,
            onRiskMomentsChange = {},
            onContinue = {},
            onBack = {}
        )
    }
}

@Preview(name = "Momentos · oscuro")
@Composable
private fun MomentsScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        MomentsScreen(
            riskMoments = setOf(RiskMoment.WAKING_UP, RiskMoment.BORED),
            canContinue = true,
            onRiskMomentsChange = {},
            onContinue = {},
            onBack = {}
        )
    }
}
