package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.EmptyState
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons

@Composable
internal fun EmptyStateSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_empty_state)) {
        EmptyState(
            title = stringResource(R.string.catalog_empty_title),
            message = stringResource(R.string.catalog_empty_message),
            modifier = Modifier.fillMaxWidth(),
            icon = SegundoAireIcons.Device,
            actionLabel = stringResource(R.string.catalog_empty_action),
            onAction = {}
        )
    }
}
