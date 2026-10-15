package com.segundoaire.presentation.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * Limita la escala de fuente del sistema a [max] dentro de [content].
 *
 * Solo para texto que vive en un contenedor de tamaño fijo (el número del
 * TimerRing, las etiquetas de la barra inferior), donde crecer sin límite lo
 * sacaría del contenedor. El texto corrido no lo usa: ahí la fuente grande se
 * respeta entera y el contenido se desplaza.
 */
@Composable
fun LimitFontScale(max: Float, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    if (density.fontScale <= max) {
        content()
    } else {
        CompositionLocalProvider(
            LocalDensity provides Density(density.density, fontScale = max),
            content = content
        )
    }
}
