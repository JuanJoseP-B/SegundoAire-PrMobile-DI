package com.segundoaire.presentation.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

private val RangeTrackWidth = 2.dp
private val RangeFillWidth = 4.dp
private val RangeStopRadius = 3.dp
private val RangeThumbRadius = 10.dp

/**
 * Selector de un tramo dentro de una escala ordenada (por ejemplo, horas de uso
 * al día). Cada parada es un botón de radio de 48 dp: se elige tocándola, sin
 * arrastre, así que funciona igual con TalkBack y teclado. El pulgar se mueve
 * con `Motion.toggle`; con reducir movimiento salta a la parada.
 */
@Composable
fun RangeSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val typography = SegundoAireTheme.typography
    val motion = SegundoAireTheme.motion
    val reduced = motion.reduced
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val animatedPosition by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = motion.toggle(),
        label = "rangeThumb"
    )
    val active = if (enabled) colors.primary else colors.disabledContent
    val track = if (enabled) colors.outline else colors.disabledContent

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.space2)
    ) {
        AnimatedContent(
            targetState = options.getOrNull(selectedIndex).orEmpty(),
            transitionSpec = { fadeIn(motion.toggle()) togetherWith fadeOut(motion.toggle()) },
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth(),
            label = "rangeLabel"
        ) { label ->
            Text(
                text = label,
                style = typography.titleLg,
                color = if (enabled) colors.onBackground else colors.disabledContent,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (options.isEmpty()) return@drawBehind
                    val cell = size.width / options.size
                    fun stopX(position: Float): Float {
                        val x = cell * (position + 0.5f)
                        return if (rtl) size.width - x else x
                    }
                    val y = size.height / 2
                    val position = if (reduced) selectedIndex.toFloat() else animatedPosition
                    val thumbX = stopX(position)

                    drawLine(
                        color = track,
                        start = Offset(stopX(0f), y),
                        end = Offset(stopX(options.lastIndex.toFloat()), y),
                        strokeWidth = RangeTrackWidth.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = active,
                        start = Offset(stopX(0f), y),
                        end = Offset(thumbX, y),
                        strokeWidth = RangeFillWidth.toPx(),
                        cap = StrokeCap.Round
                    )
                    options.indices.forEach { index ->
                        drawCircle(
                            color = if (index <= position) active else track,
                            radius = RangeStopRadius.toPx(),
                            center = Offset(stopX(index.toFloat()), y)
                        )
                    }
                    drawCircle(color = active, radius = RangeThumbRadius.toPx(), center = Offset(thumbX, y))
                }
                .selectableGroup()
        ) {
            options.forEachIndexed { index, option ->
                val source = remember { MutableInteractionSource() }
                Box(
                    Modifier
                        .weight(1f)
                        .height(ButtonMinSize)
                        .focusRing(source, SegundoAireTheme.shapes.radiusFull)
                        .selectable(
                            selected = index == selectedIndex,
                            interactionSource = source,
                            indication = null,
                            enabled = enabled,
                            role = Role.RadioButton,
                            onClick = { onSelected(index) }
                        )
                        .semantics { contentDescription = option }
                )
            }
        }

        if (options.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val captionColor = if (enabled) colors.onBackgroundMuted else colors.disabledContent
                Text(text = options.first(), style = typography.labelSm, color = captionColor)
                Text(text = options.last(), style = typography.labelSm, color = captionColor)
            }
        }
    }
}

@Composable
private fun RangeSelectorSample() {
    val spacing = SegundoAireTheme.spacing
    val options = listOf(25, 50, 90, 120).map { stringResource(R.string.timer_duration_option, it) }
    Column(
        modifier = Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(spacing.space6),
        verticalArrangement = Arrangement.spacedBy(spacing.space6)
    ) {
        RangeSelector(options = options, selectedIndex = 2, onSelected = {})
        RangeSelector(options = options, selectedIndex = 0, onSelected = {}, enabled = false)
    }
}

@Preview(name = "RangeSelector · claro")
@Composable
private fun RangeSelectorLightPreview() {
    SegundoAireTheme(darkTheme = false) { RangeSelectorSample() }
}

@Preview(name = "RangeSelector · oscuro")
@Composable
private fun RangeSelectorDarkPreview() {
    SegundoAireTheme(darkTheme = true) { RangeSelectorSample() }
}
