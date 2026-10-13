package com.segundoaire.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.domain.model.Goal
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.components.HoldToCommitButton
import com.segundoaire.presentation.designsystem.components.QuestionScaffold
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/**
 * Paso 8: el compromiso. Se confirma manteniendo presionado el botón, que
 * vibra al completarse (la vibración la da el propio [HoldToCommitButton]).
 */
@Composable
fun CommitmentScreen(
    name: String,
    goals: List<Goal>,
    canCommit: Boolean,
    onCommit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val typography = SegundoAireTheme.typography

    QuestionScaffold(
        action = {
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.space3),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.onboarding_commitment_hint),
                    style = typography.bodyMd,
                    color = colors.onBackgroundMuted,
                    textAlign = TextAlign.Center
                )
                HoldToCommitButton(
                    text = stringResource(R.string.onboarding_commitment_hold),
                    onCommit = onCommit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canCommit
                )
            }
        },
        modifier = modifier,
        title = stringResource(R.string.onboarding_commitment_title, name),
        subtitle = stringResource(R.string.onboarding_commitment_subtitle),
        step = OnboardingStep.COMMITMENT.progress,
        totalSteps = OnboardingStep.PROGRESS_STEPS,
        onBack = onBack
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(spacing.space6)
        ) {
            Text(
                text = stringResource(R.string.onboarding_commitment_pledge),
                style = typography.titleLg,
                color = colors.onBackground
            )
            if (goals.isNotEmpty()) {
                Spacer(Modifier.height(spacing.space6))
                OnboardingEyebrow(stringResource(R.string.onboarding_commitment_goals_eyebrow))
                Spacer(Modifier.height(spacing.space2))
                goals.forEach { goal ->
                    Text(
                        text = stringResource(goal.labelRes),
                        style = typography.bodyLg,
                        color = colors.onBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun CommitmentScreenSample(canCommit: Boolean) {
    CommitmentScreen(
        name = stringResource(R.string.onboarding_name_placeholder),
        goals = listOf(Goal.SLEEP, Goal.FOCUS),
        canCommit = canCommit,
        onCommit = {},
        onBack = {}
    )
}

@Preview(name = "Compromiso · claro")
@Composable
private fun CommitmentScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) { CommitmentScreenSample(canCommit = true) }
}

@Preview(name = "Compromiso · oscuro")
@Composable
private fun CommitmentScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) { CommitmentScreenSample(canCommit = false) }
}
