package com.segundoaire.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.RiskMoment
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.components.QuestionScaffold
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/**
 * Paso 7: el plan que sale de las respuestas. Tres tarjetas: objetivos, rigor
 * sugerido (con su porqué) y horarios de riesgo.
 */
@Composable
fun PlanScreen(
    name: String,
    goals: List<Goal>,
    suggestedRigor: RigorLevel,
    riskMoments: Set<RiskMoment>,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val rigor = suggestedRigor.toRigor()

    QuestionScaffold(
        primaryLabel = stringResource(R.string.onboarding_continue),
        onPrimary = onContinue,
        modifier = modifier,
        title = stringResource(R.string.onboarding_plan_title, name),
        subtitle = stringResource(R.string.onboarding_plan_subtitle),
        step = OnboardingStep.PLAN.progress,
        totalSteps = OnboardingStep.PROGRESS_STEPS,
        onBack = onBack
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(SegundoAireTheme.spacing.space4)) {
            PlanCard(
                eyebrow = pluralStringResource(R.plurals.onboarding_plan_goals_eyebrow, goals.size)
            ) {
                goals.forEach { goal ->
                    Text(
                        text = stringResource(goal.labelRes),
                        style = typography.titleLg,
                        color = colors.onBackground
                    )
                }
            }

            PlanCard(eyebrow = stringResource(R.string.onboarding_plan_rigor_eyebrow)) {
                Text(
                    text = stringResource(rigor.labelRes),
                    style = typography.titleLg,
                    color = colors.onBackground
                )
                Text(
                    text = stringResource(rigor.descriptionRes),
                    style = typography.bodyMd,
                    color = colors.onBackground
                )
                Text(
                    text = stringResource(
                        when (suggestedRigor) {
                            RigorLevel.MINDFUL -> R.string.onboarding_plan_rigor_mindful_reason
                            RigorLevel.STRICT -> R.string.onboarding_plan_rigor_strict_reason
                        }
                    ),
                    style = typography.bodyMd,
                    color = colors.onBackground
                )
            }

            PlanCard(eyebrow = stringResource(R.string.onboarding_plan_moments_eyebrow)) {
                // En el orden del día, no en el que se eligieron
                RiskMoment.entries.filter { it in riskMoments }.forEach { moment ->
                    Text(
                        text = stringResource(moment.labelRes),
                        style = typography.titleMd,
                        color = colors.onBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanCard(
    eyebrow: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = SegundoAireTheme.spacing
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
            OnboardingEyebrow(eyebrow)
            content()
        }
    }
}

@Composable
private fun PlanScreenSample(suggestedRigor: RigorLevel) {
    PlanScreen(
        name = stringResource(R.string.onboarding_name_placeholder),
        goals = listOf(Goal.SLEEP, Goal.FOCUS),
        suggestedRigor = suggestedRigor,
        riskMoments = setOf(RiskMoment.BEFORE_SLEEP, RiskMoment.BORED),
        onContinue = {},
        onBack = {}
    )
}

@Preview(name = "Plan · claro")
@Composable
private fun PlanScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) { PlanScreenSample(RigorLevel.MINDFUL) }
}

@Preview(name = "Plan · oscuro")
@Composable
private fun PlanScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) { PlanScreenSample(RigorLevel.STRICT) }
}
