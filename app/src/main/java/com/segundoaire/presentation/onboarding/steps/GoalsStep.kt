package com.segundoaire.presentation.onboarding.steps

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.GoalOptions
import com.segundoaire.presentation.onboarding.OnboardingOption
import com.segundoaire.presentation.onboarding.OnboardingRules
import com.segundoaire.presentation.onboarding.StepButton
import com.segundoaire.presentation.onboarding.StepContentSide
import com.segundoaire.presentation.onboarding.StepHeader

@Composable
internal fun GoalsText(step: Int, total: Int, name: String) {
    val trimmed = name.trim()
    StepHeader(
        eyebrow = stringResource(R.string.onb_goals_eyebrow, step, total),
        title = if (trimmed.isEmpty()) {
            stringResource(R.string.onb_goals_title)
        } else {
            stringResource(R.string.onb_goals_title_named, trimmed)
        }
    )
}

@Composable
internal fun GoalsContent(
    picks: List<Int>,
    shown: Boolean,
    shakeIndex: Int,
    onPick: (Int) -> Unit,
    onContinue: () -> Unit
) {
    val full = picks.size >= OnboardingRules.MaxGoals
    val hint = animateFloatAsState(
        targetValue = if (full) 1f else 0f,
        animationSpec = SegundoAireTheme.motion.fade(300),
        label = "goalsHint"
    )

    Column(
        Modifier
            .offset(x = StepContentSide, y = 238.dp)
            .width(358.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GoalOptions.forEachIndexed { index, option ->
            val selected = index in picks
            OnboardingOption(
                label = stringResource(option.label),
                iconPath = option.icon,
                selected = selected,
                onClick = { onPick(index) },
                shown = shown,
                index = index,
                role = Role.Checkbox,
                dimmed = full && !selected,
                shaking = shakeIndex == index,
                filledIcon = option.filledIcon
            )
        }
        Text(
            text = stringResource(R.string.onb_goals_hint),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .graphicsLayer { alpha = hint.value },
            style = SegundoAireTheme.typography.bodyMd.copy(fontSize = 13.sp),
            color = SegundoAireTheme.colors.onBackgroundMuted,
            textAlign = TextAlign.Center
        )
    }
    StepButton(
        text = stringResource(R.string.onboarding_continue),
        onClick = onContinue,
        enabled = picks.isNotEmpty()
    )
}
