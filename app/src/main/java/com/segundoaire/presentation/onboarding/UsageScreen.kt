package com.segundoaire.presentation.onboarding

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.domain.model.UsageRange
import com.segundoaire.presentation.designsystem.components.QuestionScaffold
import com.segundoaire.presentation.designsystem.components.RangeSelector
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/** Paso 3: uso diario estimado, en cinco tramos. */
@Composable
fun UsageScreen(
    usageRange: UsageRange,
    onUsageRangeChange: (UsageRange) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val options = UsageRange.entries
    QuestionScaffold(
        primaryLabel = stringResource(R.string.onboarding_continue),
        onPrimary = onContinue,
        modifier = modifier,
        title = stringResource(R.string.onboarding_usage_title),
        subtitle = stringResource(R.string.onboarding_usage_subtitle),
        step = OnboardingStep.USAGE.progress,
        totalSteps = OnboardingStep.PROGRESS_STEPS,
        onBack = onBack
    ) {
        Spacer(Modifier.height(SegundoAireTheme.spacing.space8))
        RangeSelector(
            options = options.map { stringResource(it.labelRes) },
            selectedIndex = usageRange.ordinal,
            onSelected = { onUsageRangeChange(options[it]) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Uso diario · claro")
@Composable
private fun UsageScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) {
        UsageScreen(
            usageRange = UsageRange.FROM_4_TO_6,
            onUsageRangeChange = {},
            onContinue = {},
            onBack = {}
        )
    }
}

@Preview(name = "Uso diario · oscuro")
@Composable
private fun UsageScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        UsageScreen(
            usageRange = UsageRange.OVER_8,
            onUsageRangeChange = {},
            onContinue = {},
            onBack = {}
        )
    }
}
