package com.segundoaire.presentation.onboarding.steps

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.StepButton
import com.segundoaire.presentation.onboarding.StepHeader
import com.segundoaire.presentation.onboarding.UsageCaptions
import com.segundoaire.presentation.onboarding.UsageLabels
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

private const val NodeSpacing = 77.5f
private const val ThumbSize = 36f
private const val WavePath = "M-8 12 C60 4 120 16 160 10 S260 4 318 10"
private const val ThumbPath = "M3 12 C6 4 18 4 21 12 C18 20 6 20 3 12Z"

@Composable
internal fun UsageText(step: Int, total: Int) {
    StepHeader(
        eyebrow = stringResource(R.string.onb_step_of, step, total),
        title = stringResource(R.string.onb_usage_title),
        subtitle = stringResource(R.string.onb_usage_subtitle)
    )
}

@Composable
internal fun UsageContent(
    use: Int,
    onPick: (Int) -> Unit,
    onContinue: () -> Unit
) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val motion = SegundoAireTheme.motion

    // Cifra grande del rango elegido
    Column(
        Modifier
            .offset(y = 268.dp)
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(UsageLabels[use]),
            style = typography.serif(size = 56, lineHeight = 60, tracking = (-0.02).em),
            color = colors.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.onb_usage_per_day),
            modifier = Modifier.offset(y = 4.dp),
            style = typography.bodyLg.copy(fontSize = 15.sp, lineHeight = 22.sp),
            color = colors.onBackgroundMuted
        )
    }

    // Rama con hoja-pulgar: cinco nodos con role=radio
    val wave = remember { PathParser().parsePathString(WavePath).toPath() }
    val thumb = remember { PathParser().parsePathString(ThumbPath).toPath() }
    val groupLabel = stringResource(R.string.onb_usage_group)
    val thumbX = animateFloatAsState(
        targetValue = use * NodeSpacing - ThumbSize / 2f,
        animationSpec = motion.bloom(true, 520),
        label = "thumb"
    )
    Box(
        Modifier
            .offset(x = 40.dp, y = 420.dp)
            .width(310.dp)
            .selectableGroup()
            .semantics { contentDescription = groupLabel }
    ) {
        Canvas(Modifier.offset(y = (-10).dp).size(310.dp, 20.dp).clearAndSetSemantics { }) {
            scale(density, Offset.Zero) {
                drawPath(wave, colors.primary, style = Stroke(width = 3f, cap = StrokeCap.Round))
            }
        }
        Canvas(
            Modifier
                .offset(y = (-40).dp)
                .size(ThumbSize.dp)
                .graphicsLayer {
                    translationX = thumbX.value * density
                    rotationZ = -35f
                }
                .clearAndSetSemantics { }
        ) {
            scale(this.size.minDimension / 24f, Offset.Zero) {
                drawPath(thumb, colors.glow)
                drawLine(colors.blossom, Offset(3f, 12f), Offset(19f, 12f), strokeWidth = 1f)
            }
        }
        repeat(UsageLabels.size) { index ->
            UsageNode(
                index = index,
                selected = index == use,
                reached = index <= use,
                label = stringResource(UsageLabels[index]),
                onClick = { onPick(index) }
            )
        }
        repeat(UsageCaptions.size) { index ->
            Text(
                text = stringResource(UsageCaptions[index]),
                modifier = Modifier
                    .offset(x = (index * NodeSpacing - 24f).dp, y = 34.dp)
                    .width(48.dp)
                    .clearAndSetSemantics { },
                style = typography.labelMd.copy(fontWeight = FontWeight.W600),
                color = if (index == use) colors.primary else colors.onBackgroundMuted,
                textAlign = TextAlign.Center
            )
        }
    }
    StepButton(text = stringResource(R.string.onboarding_continue), onClick = onContinue)
}

@Composable
private fun UsageNode(index: Int, selected: Boolean, reached: Boolean, label: String, onClick: () -> Unit) {
    val colors = SegundoAireTheme.colors
    val dot = animateColorAsState(
        targetValue = if (reached) colors.primary else colors.blossom,
        animationSpec = SegundoAireTheme.motion.fade(300),
        label = "usageDot"
    )
    Box(
        Modifier
            .offset(x = (index * NodeSpacing - 24f).dp, y = (-24).dp)
            .size(48.dp)
            .selectable(
                selected = selected,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.RadioButton,
                onClick = onClick
            )
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(12.dp)
                .background(dot.value, CircleShape)
                .border(2.dp, colors.primary, CircleShape)
        )
    }
}
