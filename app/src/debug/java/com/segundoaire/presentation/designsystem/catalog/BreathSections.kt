package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.BreathingCircle

@Composable
internal fun BreathingCircleSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_breathing_circle)) {
        BreathingCircle(modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}
