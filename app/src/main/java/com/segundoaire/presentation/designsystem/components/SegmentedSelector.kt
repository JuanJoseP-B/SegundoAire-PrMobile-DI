package com.segundoaire.presentation.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass

// El margen del contenedor no es táctil: el segmento mismo mide 48 dp (§10)
private val SegmentMinHeight = 48.dp

/**
 * Selector de una opción entre pocas, con un indicador `primary` que se desliza
 * con `Motion.toggle`. Con reducir movimiento el indicador no se desplaza: se
 * funde en el segmento nuevo. Semántica de grupo de radio (§10).
 */
@Composable
fun SegmentedSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val motion = SegundoAireTheme.motion
    val shape = SegundoAireTheme.shapes.radiusFull
    val reduced = motion.reduced
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val indicatorColor = if (enabled) colors.primary else colors.disabledContainer
    val position by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = motion.toggle(),
        label = "segmentedIndicator"
    )

    Row(
        modifier = modifier
            .glass(GlassLevel.Level1, shape)
            .padding(spacing.space1)
            .drawBehind {
                if (reduced || options.isEmpty()) return@drawBehind
                val segmentWidth = size.width / options.size
                val start = position * segmentWidth
                drawRoundRect(
                    color = indicatorColor,
                    topLeft = Offset(if (rtl) size.width - start - segmentWidth else start, 0f),
                    size = Size(segmentWidth, size.height),
                    cornerRadius = CornerRadius(size.height / 2)
                )
            }
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            val source = remember { MutableInteractionSource() }
            val selection by animateFloatAsState(
                targetValue = if (selected) 1f else 0f,
                animationSpec = motion.toggle(),
                label = "segmentSelection"
            )
            val stateLayer by animateStateLayer(source, enabled && !selected)
            val idle = if (enabled) colors.onBackground else colors.disabledContent
            val active = if (enabled) colors.onPrimary else colors.disabledContent

            Box(
                modifier = Modifier
                    .weight(1f)
                    .focusRing(source, shape)
                    .drawBehind {
                        val radius = CornerRadius(size.height / 2)
                        if (reduced) {
                            drawRoundRect(
                                color = indicatorColor.copy(
                                    alpha = indicatorColor.alpha * selection.coerceIn(0f, 1f)
                                ),
                                cornerRadius = radius
                            )
                        }
                        drawRoundRect(color = stateLayer, cornerRadius = radius)
                    }
                    .selectable(
                        selected = selected,
                        interactionSource = source,
                        indication = null,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = { onSelected(index) }
                    )
                    .defaultMinSize(minHeight = SegmentMinHeight)
                    .padding(horizontal = spacing.space3),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = SegundoAireTheme.typography.labelMd,
                    color = lerp(idle, active, selection.coerceIn(0f, 1f)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SegmentedSelectorSample() {
    val spacing = SegundoAireTheme.spacing
    val options = listOf(25, 50, 90).map { stringResource(R.string.timer_duration_option, it) }
    Column(
        modifier = Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(spacing.space6),
        verticalArrangement = Arrangement.spacedBy(spacing.space4)
    ) {
        SegmentedSelector(options = options, selectedIndex = 0, onSelected = {}, modifier = Modifier.fillMaxWidth())
        SegmentedSelector(
            options = options,
            selectedIndex = 2,
            onSelected = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )
    }
}

@Preview(name = "SegmentedSelector · claro")
@Composable
private fun SegmentedSelectorLightPreview() {
    SegundoAireTheme(darkTheme = false) { SegmentedSelectorSample() }
}

@Preview(name = "SegmentedSelector · oscuro")
@Composable
private fun SegmentedSelectorDarkPreview() {
    SegundoAireTheme(darkTheme = true) { SegmentedSelectorSample() }
}
