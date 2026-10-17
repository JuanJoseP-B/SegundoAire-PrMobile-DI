package com.segundoaire.presentation.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.onboarding.MomentOptions
import com.segundoaire.presentation.onboarding.OnboardingOption
import com.segundoaire.presentation.onboarding.StepButton
import com.segundoaire.presentation.onboarding.StepContentSide
import com.segundoaire.presentation.onboarding.StepHeader

@Composable
internal fun MomentText(step: Int, total: Int) {
    StepHeader(
        eyebrow = stringResource(R.string.onb_step_of, step, total),
        title = stringResource(R.string.onb_moment_title),
        subtitle = stringResource(R.string.onb_moment_subtitle)
    )
}

@Composable
internal fun MomentContent(
    moment: Int,
    shown: Boolean,
    onPick: (Int) -> Unit,
    onContinue: () -> Unit
) {
    val groupLabel = stringResource(R.string.onb_moment_group)
    Column(
        Modifier
            .offset(x = StepContentSide, y = 262.dp)
            .width(358.dp)
            .selectableGroup()
            .semantics { contentDescription = groupLabel },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MomentOptions.forEachIndexed { index, option ->
            OnboardingOption(
                label = stringResource(option.label),
                iconPath = option.icon,
                selected = index == moment,
                onClick = { onPick(index) },
                shown = shown,
                index = index,
                role = Role.RadioButton,
                filledIcon = false,
                trailing = stringResource(option.hours)
            )
        }
    }
    StepButton(
        text = stringResource(R.string.onboarding_continue),
        onClick = onContinue,
        enabled = moment >= 0
    )
}
