package com.segundoaire.presentation.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/**
 * Squish de DESIGN_SYSTEM.md §8: encoge el elemento mientras [interactionSource]
 * está presionado, con `Motion.press`. Solo observa la interacción; el gesto lo
 * sigue manejando el `clickable` que comparte la misma fuente, así que no
 * afecta al click ni a la accesibilidad. Con reducir movimiento no escala.
 */
@Composable
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    enabled: Boolean = true
): Modifier {
    val motion = SegundoAireTheme.motion
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && pressed) motion.pressedScale else 1f,
        animationSpec = motion.press(),
        label = "pressScale"
    )
    // La escala se lee en la fase de dibujo: no recompone ni vuelve a medir
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
private fun PressScaleSample() {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        Modifier
            .background(colors.background)
            .padding(spacing.space6)
    ) {
        Box(
            Modifier
                .size(spacing.space16)
                .pressScale(interactionSource)
                .clip(SegundoAireTheme.shapes.radiusLg)
                .background(colors.primary)
                .clickable(interactionSource = interactionSource, indication = null, onClick = {})
        )
    }
}

@Preview(name = "pressScale · claro")
@Composable
private fun PressScaleLightPreview() {
    SegundoAireTheme(darkTheme = false) { PressScaleSample() }
}

@Preview(name = "pressScale · oscuro")
@Composable
private fun PressScaleDarkPreview() {
    SegundoAireTheme(darkTheme = true) { PressScaleSample() }
}
