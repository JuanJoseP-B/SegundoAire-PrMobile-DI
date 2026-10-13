package com.segundoaire.presentation.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.BreathingCircle
import com.segundoaire.presentation.designsystem.components.QuestionScaffold
import com.segundoaire.presentation.designsystem.theme.Motion
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import kotlinx.coroutines.delay

/**
 * Paso 6: demo de la pausa, con tres ciclos de respiración. El botón nunca
 * bloquea: mientras dura la demo ofrece saltarla y al terminar pasa a "Continuar".
 */
@Composable
fun BreathingScreen(
    cyclesDone: Int,
    onCycleCompleted: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCycles = OnboardingUiState.BREATH_CYCLES
    val done = cyclesDone >= totalCycles
    val currentOnCycleCompleted by rememberUpdatedState(onCycleCompleted)

    // Cada ciclo dura lo mismo que el del círculo: tiempo real, también con reducir movimiento
    LaunchedEffect(Unit) {
        repeat(totalCycles - cyclesDone) {
            delay((Motion.BreathInhaleMillis + Motion.BreathExhaleMillis).toLong())
            currentOnCycleCompleted()
        }
    }

    QuestionScaffold(
        primaryLabel = stringResource(
            if (done) R.string.onboarding_continue else R.string.onboarding_breathing_skip
        ),
        onPrimary = onContinue,
        modifier = modifier,
        title = stringResource(R.string.onboarding_breathing_title),
        subtitle = stringResource(R.string.onboarding_breathing_subtitle),
        step = OnboardingStep.BREATHING.progress,
        totalSteps = OnboardingStep.PROGRESS_STEPS,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SegundoAireTheme.spacing.space6),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BreathingCircle()
            val status = if (done) {
                stringResource(R.string.onboarding_breathing_done)
            } else {
                stringResource(R.string.onboarding_breathing_cycle, cyclesDone + 1, totalCycles)
            }
            Crossfade(
                targetState = status,
                animationSpec = SegundoAireTheme.motion.calm(),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                label = "breathingStatus"
            ) { text ->
                Text(
                    text = text,
                    style = SegundoAireTheme.typography.bodyMd,
                    color = SegundoAireTheme.colors.onBackgroundMuted
                )
            }
        }
    }
}

@Preview(name = "Respiración · claro")
@Composable
private fun BreathingScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) {
        BreathingScreen(cyclesDone = 0, onCycleCompleted = {}, onContinue = {}, onBack = {})
    }
}

@Preview(name = "Respiración · oscuro")
@Composable
private fun BreathingScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        BreathingScreen(
            cyclesDone = OnboardingUiState.BREATH_CYCLES,
            onCycleCompleted = {},
            onContinue = {},
            onBack = {}
        )
    }
}
