package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.SegmentedSelector

@Composable
internal fun SegmentedSelectorSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_segmented_selector)) {
        val options = listOf(25, 50, 90, 120).map { stringResource(R.string.timer_duration_option, it) }
        var selected by rememberSaveable { mutableIntStateOf(0) }
        SegmentedSelector(
            options = options,
            selectedIndex = selected,
            onSelected = { selected = it },
            modifier = Modifier.fillMaxWidth()
        )
        SegmentedSelector(
            options = options,
            selectedIndex = 1,
            onSelected = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )
    }
}
