package com.segundoaire.presentation.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

// Área táctil mínima de DESIGN_SYSTEM.md §6
internal val ButtonMinSize = 48.dp
private val ButtonIconSize = 20.dp

/**
 * Esqueleto común de los botones: squish, anillo de foco, click sin ripple
 * (el estado presionado lo muestran el color y la escala) y área táctil
 * mínima. [surface] pinta el fondo de cada variante con forma `radiusFull`.
 */
@Composable
internal fun ButtonLayout(
    onClick: () -> Unit,
    enabled: Boolean,
    interactionSource: MutableInteractionSource,
    surface: Modifier,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val spacing = SegundoAireTheme.spacing
    Box(
        modifier = modifier
            .pressScale(interactionSource, enabled)
            .focusRing(interactionSource, SegundoAireTheme.shapes.radiusFull)
            .then(surface)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .defaultMinSize(minWidth = ButtonMinSize, minHeight = ButtonMinSize)
            .padding(horizontal = spacing.space6, vertical = spacing.space3),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/** Texto `labelLg` con ícono opcional a la izquierda. El ícono es decorativo. */
@Composable
internal fun ButtonLabel(
    text: String,
    icon: ImageVector?,
    color: Color,
    iconTint: Color = color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(SegundoAireTheme.spacing.space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(ButtonIconSize)
            )
        }
        Text(
            text = text,
            style = SegundoAireTheme.typography.labelLg,
            color = color,
            textAlign = TextAlign.Center,
            // Con fuente grande la etiqueta baja de línea en vez de cortarse
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

/** Capa `stateLayer` para hover y pressed sobre superficies neutras (§3.2). */
@Composable
internal fun animateStateLayer(
    interactionSource: InteractionSource,
    enabled: Boolean
): State<Color> {
    val stateLayer = SegundoAireTheme.colors.stateLayer
    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    return animateColorAsState(
        targetValue = if (enabled && (pressed || hovered)) stateLayer else stateLayer.copy(alpha = 0f),
        animationSpec = SegundoAireTheme.motion.press(),
        label = "stateLayer"
    )
}
