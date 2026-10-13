package com.segundoaire.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/**
 * Marcador de contenido que aún carga; el tamaño lo da [modifier]. Es estático
 * a propósito: en el design system solo la respiración y el brillo del timer se
 * repiten solos (§1). No se anuncia a los lectores de pantalla.
 */
@Composable
fun Skeleton(
    modifier: Modifier = Modifier,
    shape: Shape = SegundoAireTheme.shapes.radiusXs
) {
    Box(
        modifier
            .clip(shape)
            .background(SegundoAireTheme.colors.disabledContainer)
            .clearAndSetSemantics {}
    )
}

@Composable
private fun SkeletonSample() {
    val spacing = SegundoAireTheme.spacing
    Row(
        modifier = Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(spacing.space6),
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

@Preview(name = "Skeleton · claro")
@Composable
private fun SkeletonLightPreview() {
    SegundoAireTheme(darkTheme = false) { SkeletonSample() }
}

@Preview(name = "Skeleton · oscuro")
@Composable
private fun SkeletonDarkPreview() {
    SegundoAireTheme(darkTheme = true) { SkeletonSample() }
}
