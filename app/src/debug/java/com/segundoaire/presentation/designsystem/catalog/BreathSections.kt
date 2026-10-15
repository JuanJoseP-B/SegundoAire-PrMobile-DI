package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.BreathingCircle
import com.segundoaire.presentation.designsystem.components.HoldToCommitButton

@Composable
internal fun BreathingCircleSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_breathing_circle)) {
        BreathingCircle(modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
internal fun HoldToCommitButtonSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_hold_to_commit)) {
        var commits by rememberSaveable { mutableIntStateOf(0) }
        HoldToCommitButton(
            text = stringResource(R.string.catalog_hold_label),
            onCommit = { commits++ },
            modifier = Modifier.fillMaxWidth()
        )
        CatalogCaption(stringResource(R.string.catalog_hold_count, commits))
        HoldToCommitButton(
            text = stringResource(R.string.catalog_state_disabled),
            onCommit = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )
    }
}
