package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.PermissionBanner

@Composable
internal fun PermissionBannerSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_permission_banner)) {
        PermissionBanner(
            title = stringResource(R.string.banner_title),
            message = stringResource(R.string.banner_overlay_disabled),
            actionLabel = stringResource(R.string.banner_action),
            onAction = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
