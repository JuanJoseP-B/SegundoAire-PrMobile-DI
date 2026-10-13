package com.segundoaire.presentation.designsystem.components

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/** Nivel de rigor del GlassWall. Los nombres no se traducen. */
enum class Rigor(
    @StringRes internal val labelRes: Int,
    @StringRes internal val descriptionRes: Int
) {
    Mindful(R.string.rigor_mindful, R.string.timer_rigor_mindful_desc),
    Strict(R.string.rigor_strict, R.string.timer_rigor_strict_desc)
}

/**
 * Selector Mindful / Strict con la descripción del nivel elegido debajo. La
 * descripción entra con `Motion.step` desde el lado del segmento nuevo; con
 * reducir movimiento solo se funde.
 */
@Composable
fun RigorSelector(
    selected: Rigor,
    onSelected: (Rigor) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val motion = SegundoAireTheme.motion
    val offset = with(LocalDensity.current) { motion.stepOffset.roundToPx() }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SegundoAireTheme.spacing.space3)
    ) {
        SegmentedSelector(
            options = Rigor.entries.map { stringResource(it.labelRes) },
            selectedIndex = selected.ordinal,
            onSelected = { onSelected(Rigor.entries[it]) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )
        AnimatedContent(
            targetState = selected,
            transitionSpec = {
                val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                (slideInHorizontally(motion.step()) { offset * direction } + fadeIn(motion.step())) togetherWith
                    (slideOutHorizontally(motion.step()) { -offset * direction } + fadeOut(motion.step()))
            },
            label = "rigorDescription"
        ) { rigor ->
            Text(
                text = stringResource(rigor.descriptionRes),
                style = SegundoAireTheme.typography.bodyMd,
                color = if (enabled) {
                    SegundoAireTheme.colors.onBackgroundMuted
                } else {
                    SegundoAireTheme.colors.disabledContent
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RigorSelectorSample() {
    val spacing = SegundoAireTheme.spacing
    Column(
        modifier = Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(spacing.space6),
        verticalArrangement = Arrangement.spacedBy(spacing.space6)
    ) {
        RigorSelector(selected = Rigor.Mindful, onSelected = {})
        RigorSelector(selected = Rigor.Strict, onSelected = {})
    }
}

@Preview(name = "RigorSelector · claro")
@Composable
private fun RigorSelectorLightPreview() {
    SegundoAireTheme(darkTheme = false) { RigorSelectorSample() }
}

@Preview(name = "RigorSelector · oscuro")
@Composable
private fun RigorSelectorDarkPreview() {
    SegundoAireTheme(darkTheme = true) { RigorSelectorSample() }
}
