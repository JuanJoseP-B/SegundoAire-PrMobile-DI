package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.AppIconPlaceholder
import com.segundoaire.presentation.designsystem.components.AppToggleRow

@Composable
internal fun AppToggleRowSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_app_toggle_row)) {
        val checked = remember { mutableStateListOf(true, false, true, false, true) }
        checked.forEachIndexed { index, value ->
            AppToggleRow(
                name = stringResource(R.string.catalog_sample_app, index + 1),
                checked = value,
                onCheckedChange = { checked[index] = it },
                modifier = Modifier.fillMaxWidth(),
                supportingText = if (index == 0) stringResource(R.string.catalog_sample_supporting) else null
            ) { AppIconPlaceholder(index = index) }
        }
        AppToggleRow(
            name = stringResource(R.string.catalog_state_focused),
            checked = true,
            onCheckedChange = {},
            modifier = Modifier.fillMaxWidth(),
            interactionSource = rememberFocusedInteractionSource()
        ) { AppIconPlaceholder(index = 0) }
        AppToggleRow(
            name = stringResource(R.string.catalog_state_disabled),
            checked = true,
            onCheckedChange = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        ) { AppIconPlaceholder(index = 1) }
    }
}
