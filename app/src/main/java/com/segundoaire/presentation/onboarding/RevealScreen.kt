package com.segundoaire.presentation.onboarding

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.domain.model.UsageRange
import com.segundoaire.domain.usecase.CalculateScreenTimeProjectionUseCase
import com.segundoaire.domain.usecase.ScreenTimeProjection
import com.segundoaire.presentation.designsystem.components.AnimatedCounter
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.components.QuestionScaffold
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/**
 * Paso 5: la revelación. Un solo dato de impacto (los años de pantalla
 * proyectados) que cuenta hasta su valor, con la fórmula a la vista, y debajo
 * lo que se puede recuperar.
 */
@Composable
fun RevealScreen(
    name: String,
    projection: ScreenTimeProjection,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val typography = SegundoAireTheme.typography

    // Parte de 0 para que el contador cuente al entrar; al volver ya está en su valor
    var shownTenths by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(projection.screenYearsTenths) { shownTenths = projection.screenYearsTenths }

    QuestionScaffold(
        primaryLabel = stringResource(R.string.onboarding_continue),
        onPrimary = onContinue,
        modifier = modifier,
        title = stringResource(R.string.onboarding_reveal_title, name),
        step = OnboardingStep.REVEAL.progress,
        totalSteps = OnboardingStep.PROGRESS_STEPS,
        onBack = onBack
    ) {
        AnimatedCounter(value = shownTenths) { tenths -> yearsText(tenths) }
        Text(
            text = stringResource(R.string.onboarding_reveal_years_caption, projection.horizonYears),
            style = typography.bodyMd,
            color = colors.onBackgroundMuted
        )

        Spacer(Modifier.height(spacing.space6))
        GlassCard(modifier = Modifier.fillMaxWidth(), level = GlassLevel.Level1) {
            OnboardingEyebrow(stringResource(R.string.onboarding_reveal_formula_eyebrow))
            Spacer(Modifier.height(spacing.space2))
            Text(
                text = stringResource(
                    R.string.onboarding_reveal_formula,
                    projection.hoursPerDay,
                    projection.horizonYears,
                    CalculateScreenTimeProjectionUseCase.HOURS_PER_DAY,
                    yearsText(projection.screenYearsTenths)
                ),
                style = typography.bodyLg,
                color = colors.onBackground
            )
            Spacer(Modifier.height(spacing.space2))
            Text(
                text = stringResource(R.string.onboarding_reveal_formula_note, projection.hoursPerDay),
                style = typography.bodyMd,
                color = colors.onBackground
            )
        }

        Spacer(Modifier.height(spacing.space4))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(
                    R.string.onboarding_reveal_recoverable_title,
                    projection.recoverableDaysPerYear
                ),
                style = typography.titleLg,
                color = colors.onBackground
            )
            Spacer(Modifier.height(spacing.space2))
            Text(
                text = stringResource(
                    R.string.onboarding_reveal_recoverable_formula,
                    projection.recoverablePercent,
                    projection.hoursPerDay,
                    CalculateScreenTimeProjectionUseCase.DAYS_PER_YEAR,
                    CalculateScreenTimeProjectionUseCase.HOURS_PER_DAY,
                    projection.recoverableDaysPerYear
                ),
                style = typography.bodyMd,
                color = colors.onBackground
            )
        }
    }
}

// Décimas a "8,3" / "8.3" con el separador decimal del idioma
@Composable
private fun yearsText(tenths: Int): String =
    stringResource(R.string.onboarding_reveal_years_value, tenths / 10, tenths % 10)

@Composable
private fun RevealScreenSample(usageRange: UsageRange) {
    RevealScreen(
        name = stringResource(R.string.onboarding_name_placeholder),
        projection = CalculateScreenTimeProjectionUseCase()(usageRange),
        onContinue = {},
        onBack = {}
    )
}

@Preview(name = "Revelación · claro")
@Composable
private fun RevealScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) { RevealScreenSample(UsageRange.FROM_4_TO_6) }
}

@Preview(name = "Revelación · oscuro")
@Composable
private fun RevealScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) { RevealScreenSample(UsageRange.OVER_8) }
}
