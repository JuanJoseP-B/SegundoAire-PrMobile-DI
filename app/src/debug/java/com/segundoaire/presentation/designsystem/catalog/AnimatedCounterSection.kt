package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.AnimatedCounter
import com.segundoaire.presentation.designsystem.components.SecondaryButton
import com.segundoaire.presentation.designsystem.components.TextButton
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

private const val CounterStep = 23

@Composable
internal fun AnimatedCounterSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_animated_counter)) {
        var minutes by rememberSaveable { mutableIntStateOf(47) }
        AnimatedCounter(value = minutes) { stringResource(R.string.dashboard_minutes_format, it) }
        Row(horizontalArrangement = Arrangement.spacedBy(SegundoAireTheme.spacing.space2)) {
            SecondaryButton(
                text = stringResource(R.string.catalog_counter_add),
                onClick = { minutes += CounterStep }
            )
            TextButton(
                text = stringResource(R.string.catalog_counter_reset),
                onClick = { minutes = 0 }
            )
        }
    }
}
