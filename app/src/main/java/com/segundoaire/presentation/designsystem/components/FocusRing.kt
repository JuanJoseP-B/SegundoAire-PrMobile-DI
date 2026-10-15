package com.segundoaire.presentation.designsystem.components

import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

private val FocusRingWidth = 2.dp
private val FocusRingGap = 3.dp

/**
 * Anillo de foco de DESIGN_SYSTEM.md §10: 2 dp de `focusRing` a 3 dp del borde
 * del elemento, visible mientras [interactionSource] tiene el foco (teclado,
 * D-pad o accesibilidad por interruptores). Se dibuja fuera de los límites, así
 * que debe ir antes del `clip` del componente.
 */
@Composable
fun Modifier.focusRing(
    interactionSource: InteractionSource,
    shape: Shape
): Modifier {
    val focused by interactionSource.collectIsFocusedAsState()
    val color = SegundoAireTheme.colors.focusRing
    return drawWithCache {
        val stroke = Stroke(FocusRingWidth.toPx())
        val inset = FocusRingGap.toPx() + stroke.width / 2
        val outline = shape.createOutline(
            size = Size(size.width + inset * 2, size.height + inset * 2),
            layoutDirection = layoutDirection,
            density = this
        )
        onDrawWithContent {
            drawContent()
            if (focused) {
                translate(-inset, -inset) { drawOutline(outline, color, style = stroke) }
            }
        }
    }
}
