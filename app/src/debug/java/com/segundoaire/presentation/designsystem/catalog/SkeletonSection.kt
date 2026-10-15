package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.Skeleton
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

private const val SkeletonRows = 2

@Composable
internal fun SkeletonSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_skeleton)) {
        val spacing = SegundoAireTheme.spacing
        // La silueta de una fila de app mientras carga
        repeat(SkeletonRows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.space3),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Skeleton(modifier = Modifier.size(spacing.space12), shape = SegundoAireTheme.shapes.radiusSm)
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    Skeleton(
                        Modifier
                            .fillMaxWidth(0.6f)
                            .height(spacing.space4)
                    )
                    Skeleton(
                        Modifier
                            .fillMaxWidth(0.35f)
                            .height(spacing.space3)
                    )
                }
            }
        }
    }
}
