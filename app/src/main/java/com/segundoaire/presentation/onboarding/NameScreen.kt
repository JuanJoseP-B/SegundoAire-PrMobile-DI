package com.segundoaire.presentation.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.GlassTextField
import com.segundoaire.presentation.designsystem.components.QuestionScaffold
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/** Paso 1: el nombre con el que la app saluda al usuario. */
@Composable
fun NameScreen(
    name: String,
    nameError: NameError?,
    canContinue: Boolean,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    QuestionScaffold(
        primaryLabel = stringResource(R.string.onboarding_continue),
        onPrimary = onContinue,
        modifier = modifier,
        primaryEnabled = canContinue,
        title = stringResource(R.string.onboarding_name_title),
        subtitle = stringResource(R.string.onboarding_name_subtitle),
        step = OnboardingStep.NAME.progress,
        totalSteps = OnboardingStep.PROGRESS_STEPS,
        onBack = onBack
    ) {
        GlassTextField(
            value = name,
            onValueChange = onNameChange,
            label = stringResource(R.string.onboarding_name_label),
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(R.string.onboarding_name_placeholder),
            supportingText = stringResource(R.string.onboarding_name_supporting),
            errorMessage = when (nameError) {
                NameError.INVALID_CHARACTERS -> stringResource(R.string.onboarding_name_error)
                null -> null
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { if (canContinue) onContinue() })
        )
    }
}

@Preview(name = "Nombre · claro")
@Composable
private fun NameScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) {
        NameScreen(
            name = stringResource(R.string.onboarding_name_placeholder),
            nameError = null,
            canContinue = true,
            onNameChange = {},
            onContinue = {},
            onBack = {}
        )
    }
}

@Preview(name = "Nombre · oscuro")
@Composable
private fun NameScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        NameScreen(
            name = stringResource(R.string.timer_ring_preview_time),
            nameError = NameError.INVALID_CHARACTERS,
            canContinue = false,
            onNameChange = {},
            onContinue = {},
            onBack = {}
        )
    }
}
