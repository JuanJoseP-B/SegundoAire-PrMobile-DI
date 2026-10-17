package com.segundoaire.presentation.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass

private val ToggleWidth = 52.dp
private val ToggleHeight = 32.dp
// El pulgar crece de 16 a 24 dp al encenderse, como en la referencia de la app principal
private val ToggleThumbOff = 16.dp
private val ToggleThumbOn = 24.dp
private val ToggleBorderWidth = 2.dp
private val AppIconSize = 48.dp

/**
 * Fila de una app vigilada: ícono, nombre y toggle. Toda la fila es el control
 * (`Role.Switch`), así que el área táctil no depende del tamaño del toggle.
 * [icon] suele ser un [AppIconPlaceholder] o el ícono real de la app.
 */
@Composable
fun AppToggleRow(
    name: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    icon: @Composable () -> Unit
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val shape = SegundoAireTheme.shapes.radiusMd
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val stateLayer by animateStateLayer(source, enabled)

    Row(
        modifier = modifier
            .focusRing(source, shape)
            .glass(GlassLevel.Level1, shape)
            .drawBehind { drawRect(stateLayer) }
            .toggleable(
                value = checked,
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .padding(horizontal = spacing.space4, vertical = spacing.space3),
        horizontalArrangement = Arrangement.spacedBy(spacing.space3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Column(Modifier.weight(1f)) {
            Text(
                text = name,
                style = SegundoAireTheme.typography.titleMd,
                color = if (enabled) colors.onBackground else colors.disabledContent
            )
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = SegundoAireTheme.typography.bodyMd,
                    color = if (enabled) colors.onBackgroundMuted else colors.disabledContent
                )
            }
        }
        Toggle(checked = checked, enabled = enabled)
    }
}

/**
 * Indicador del toggle, sin semántica propia: el control es quien lo contiene.
 * Apagado: pista `outlineVariant` con borde muted y pulgar de 16 dp; encendido:
 * pista `primary` y pulgar `onPrimary` de 24 dp. El pulgar se desliza y crece con
 * `Motion.toggle`; con reducir movimiento no se
 * desplaza y solo se funden los colores.
 */
@Composable
internal fun Toggle(
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val fraction by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = motion.toggle(),
        label = "toggle"
    )
    val reduced = motion.reduced

    Box(
        modifier
            .size(ToggleWidth, ToggleHeight)
            .drawBehind {
                // El resorte rebota: los colores se acotan, la posición y el tamaño no
                val colorFraction = fraction.coerceIn(0f, 1f)
                val position = if (reduced) (if (checked) 1f else 0f) else fraction
                val radius = CornerRadius(size.height / 2)
                val trackOff = if (enabled) colors.outlineVariant else colors.disabledContainer
                val trackOn = if (enabled) colors.primary else colors.disabledContainer
                val thumbOff = if (enabled) colors.onBackgroundMuted else colors.disabledContent
                val thumbOn = if (enabled) colors.onPrimary else colors.disabledContent

                drawRoundRect(color = trackOff.copy(alpha = trackOff.alpha * (1f - colorFraction)), cornerRadius = radius)
                drawRoundRect(color = trackOn.copy(alpha = trackOn.alpha * colorFraction), cornerRadius = radius)
                val border = ToggleBorderWidth.toPx()
                drawRoundRect(
                    color = thumbOff.copy(alpha = thumbOff.alpha * (1f - colorFraction)),
                    topLeft = Offset(border / 2, border / 2),
                    size = size.copy(width = size.width - border, height = size.height - border),
                    cornerRadius = radius,
                    style = Stroke(border)
                )

                val offRadius = ToggleThumbOff.toPx() / 2
                val onRadius = ToggleThumbOn.toPx() / 2
                val thumbRadius = (offRadius + (onRadius - offRadius) * position).coerceAtLeast(0f)
                val edge = size.height / 2
                drawCircle(
                    color = lerp(thumbOff, thumbOn, colorFraction),
                    radius = thumbRadius,
                    center = Offset(edge + (size.width - edge * 2) * position, size.height / 2)
                )
            }
    )
}

/**
 * Ícono de app de ejemplo: una forma geométrica sobre `app1…5`, elegidas por
 * [index]. Nunca logos reales (DESIGN_SYSTEM.md §9).
 */
@Composable
fun AppIconPlaceholder(
    index: Int,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val palette = listOf(colors.app1, colors.app2, colors.app3, colors.app4, colors.app5)
    val slot = index.mod(palette.size)
    val ink = colors.background
    Box(
        modifier
            .size(AppIconSize)
            .clip(SegundoAireTheme.shapes.radiusSm)
            .background(palette[slot])
            .drawBehind {
                val unit = size.minDimension / 4
                when (slot) {
                    0 -> drawCircle(ink, radius = unit)
                    1 -> drawRoundRect(
                        color = ink,
                        topLeft = Offset(center.x - unit, center.y - unit),
                        size = size.copy(width = unit * 2, height = unit * 2),
                        cornerRadius = CornerRadius(unit / 3)
                    )
                    2 -> drawCircle(ink, radius = unit, style = Stroke(unit / 2))
                    3 -> drawRoundRect(
                        color = ink,
                        topLeft = Offset(center.x - unit * 1.25f, center.y - unit / 2),
                        size = size.copy(width = unit * 2.5f, height = unit),
                        cornerRadius = CornerRadius(unit / 2)
                    )
                    else -> {
                        drawCircle(ink, radius = unit / 2, center = Offset(center.x - unit * 0.7f, center.y))
                        drawCircle(ink, radius = unit / 2, center = Offset(center.x + unit * 0.7f, center.y))
                    }
                }
            }
    )
}

@Composable
private fun AppToggleRowSample() {
    val spacing = SegundoAireTheme.spacing
    Column(
        modifier = Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(spacing.space6),
        verticalArrangement = Arrangement.spacedBy(spacing.space3)
    ) {
        AppToggleRow(
            name = stringResource(R.string.app_name),
            checked = true,
            onCheckedChange = {},
            modifier = Modifier.fillMaxWidth(),
            supportingText = stringResource(R.string.timer_duration_option, 23)
        ) { AppIconPlaceholder(index = 0) }
        AppToggleRow(
            name = stringResource(R.string.app_name),
            checked = false,
            onCheckedChange = {},
            modifier = Modifier.fillMaxWidth()
        ) { AppIconPlaceholder(index = 1) }
        AppToggleRow(
            name = stringResource(R.string.app_name),
            checked = true,
            onCheckedChange = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        ) { AppIconPlaceholder(index = 2) }
    }
}

@Preview(name = "AppToggleRow · claro")
@Composable
private fun AppToggleRowLightPreview() {
    SegundoAireTheme(darkTheme = false) { AppToggleRowSample() }
}

@Preview(name = "AppToggleRow · oscuro")
@Composable
private fun AppToggleRowDarkPreview() {
    SegundoAireTheme(darkTheme = true) { AppToggleRowSample() }
}
