package com.segundoaire.presentation.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass

/**
 * Chip de elección: vidrio nivel 1 que se rellena de `secondary` al elegirlo,
 * con `Motion.toggle`. [role] es `Checkbox` en grupos de elección múltiple y
 * `RadioButton` en los de elección única.
 */
@Composable
fun ChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    role: Role = Role.Checkbox,
    interactionSource: MutableInteractionSource? = null
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val shape = SegundoAireTheme.shapes.radiusSm
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val stateLayer by animateStateLayer(source, enabled)
    val selection by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = SegundoAireTheme.motion.toggle(),
        label = "chipSelection"
    )
    // Un chip elegido sigue viéndose elegido aunque esté deshabilitado
    val fill = if (enabled || selected) colors.secondary else colors.disabledContainer
    val idle = if (enabled) colors.onBackground else colors.disabledContent
    val active = if (enabled || selected) colors.onSecondary else colors.disabledContent

    val selectionModifier = if (role == Role.RadioButton) {
        Modifier.selectable(
            selected = selected,
            interactionSource = source,
            indication = null,
            enabled = enabled,
            role = role,
            onClick = onClick
        )
    } else {
        Modifier.toggleable(
            value = selected,
            interactionSource = source,
            indication = null,
            enabled = enabled,
            role = role,
            onValueChange = { onClick() }
        )
    }

    Box(
        modifier = modifier
            .pressScale(source, enabled)
            .focusRing(source, shape)
            .glass(GlassLevel.Level1, shape)
            .drawBehind {
                drawRect(fill.copy(alpha = fill.alpha * selection.coerceIn(0f, 1f)))
                drawRect(stateLayer)
            }
            .then(selectionModifier)
            .defaultMinSize(minWidth = ButtonMinSize, minHeight = ButtonMinSize)
            .padding(horizontal = spacing.space4, vertical = spacing.space3),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SegundoAireTheme.typography.labelMd,
            color = lerp(idle, active, selection.coerceIn(0f, 1f))
        )
    }
}

/**
 * Grupo de chips en varias filas. Con [maxSelected] = 1 la elección es única
 * (elegir otro chip reemplaza al anterior); con un límite mayor, al alcanzarlo
 * los chips sin elegir se deshabilitan hasta que se libere uno.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChoiceChipGroup(
    options: List<String>,
    selected: Set<Int>,
    onSelectionChange: (Set<Int>) -> Unit,
    modifier: Modifier = Modifier,
    maxSelected: Int = Int.MAX_VALUE
) {
    val spacing = SegundoAireTheme.spacing
    val single = maxSelected == 1
    FlowRow(
        modifier = if (single) modifier.selectableGroup() else modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.space2),
        verticalArrangement = Arrangement.spacedBy(spacing.space2)
    ) {
        options.forEachIndexed { index, option ->
            ChoiceChip(
                text = option,
                selected = index in selected,
                onClick = { onSelectionChange(toggleChoice(selected, index, maxSelected)) },
                enabled = canToggleChoice(selected, index, maxSelected),
                role = if (single) Role.RadioButton else Role.Checkbox
            )
        }
    }
}

/** Selección resultante de tocar [index] con un máximo de [maxSelected] elegidos. */
internal fun toggleChoice(selected: Set<Int>, index: Int, maxSelected: Int): Set<Int> = when {
    maxSelected == 1 -> setOf(index)
    index in selected -> selected - index
    selected.size < maxSelected -> selected + index
    else -> selected
}

/** `false` si [index] no se puede elegir porque ya se alcanzó el límite. */
internal fun canToggleChoice(selected: Set<Int>, index: Int, maxSelected: Int): Boolean =
    maxSelected == 1 || index in selected || selected.size < maxSelected

@Composable
private fun ChoiceChipSample() {
    val spacing = SegundoAireTheme.spacing
    val options = listOf(
        stringResource(R.string.nav_panel),
        stringResource(R.string.nav_blocks),
        stringResource(R.string.timer_duration_title),
        stringResource(R.string.timer_rigor_title)
    )
    Column(
        modifier = Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(spacing.space6),
        verticalArrangement = Arrangement.spacedBy(spacing.space6)
    ) {
        ChoiceChipGroup(options = options, selected = setOf(1), onSelectionChange = {}, maxSelected = 1)
        ChoiceChipGroup(options = options, selected = setOf(0, 2), onSelectionChange = {}, maxSelected = 2)
    }
}

@Preview(name = "ChoiceChip · claro")
@Composable
private fun ChoiceChipLightPreview() {
    SegundoAireTheme(darkTheme = false) { ChoiceChipSample() }
}

@Preview(name = "ChoiceChip · oscuro")
@Composable
private fun ChoiceChipDarkPreview() {
    SegundoAireTheme(darkTheme = true) { ChoiceChipSample() }
}
