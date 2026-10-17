package com.segundoaire.presentation.onboarding.steps

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.GlassTextField
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.OnboardingRules
import com.segundoaire.presentation.onboarding.StepButton
import com.segundoaire.presentation.onboarding.StepContentSide
import com.segundoaire.presentation.onboarding.StepHeader
import com.segundoaire.presentation.onboarding.NameError

@Composable
internal fun NameText(step: Int, total: Int) {
    StepHeader(
        eyebrow = stringResource(R.string.onb_step_of, step, total),
        title = stringResource(R.string.onb_name_title),
        subtitle = stringResource(R.string.onb_name_subtitle)
    )
}

@Composable
internal fun NameContent(
    name: String,
    nameError: NameError?,
    canContinue: Boolean,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    val motion = SegundoAireTheme.motion
    val typography = SegundoAireTheme.typography
    val hasName = name.isNotBlank() && nameError == null
    val greeting = animateFloatAsState(
        targetValue = if (hasName) 1f else 0f,
        animationSpec = motion.fade(400),
        label = "greeting"
    )

    Column(
        Modifier
            .offset(x = StepContentSide, y = 268.dp)
            .width(358.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GlassTextField(
            value = name,
            onValueChange = { onNameChange(it.take(OnboardingRules.NameMaxLength)) },
            label = stringResource(R.string.onb_name_label),
            placeholder = stringResource(R.string.onb_name_placeholder),
            errorMessage = if (nameError != null) stringResource(R.string.onboarding_name_error) else null,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { if (canContinue) onContinue() })
        )
        Text(
            text = stringResource(R.string.onb_name_greeting, name.trim()),
            modifier = Modifier.graphicsLayer {
                alpha = greeting.value
                translationY = (1f - greeting.value) * 6.dp.toPx()
            },
            style = typography.serif(size = 20, lineHeight = 28, weight = androidx.compose.ui.text.font.FontWeight.W400, italic = true),
            color = SegundoAireTheme.colors.primary
        )
    }
    StepButton(
        text = stringResource(R.string.onboarding_continue),
        onClick = onContinue,
        enabled = canContinue
    )
}
