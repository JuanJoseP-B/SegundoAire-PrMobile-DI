package com.segundoaire.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass

/**
 * Tarjeta de vidrio de DESIGN_SYSTEM.md §5 con radio `radiusLg`. El [level]
 * sigue el uso de la tabla: 1 para filas, 2 para tarjetas y 3 para hojas.
 * Con [onClick] la tarjeta es presionable: squish, `stateLayer` y anillo de foco.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    level: GlassLevel = GlassLevel.Level2,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(SegundoAireTheme.spacing.space4),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = SegundoAireTheme.shapes.radiusLg
    val surface = if (onClick != null) {
        val source = interactionSource ?: remember { MutableInteractionSource() }
        val stateLayer by animateStateLayer(source, enabled)
        Modifier
            .pressScale(source, enabled)
            .focusRing(source, shape)
            .glass(level, shape)
            .drawBehind { drawRect(stateLayer) }
            .clickable(
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
    } else {
        Modifier.glass(level, shape)
    }

    Column(
        modifier = modifier
            .then(surface)
            .padding(contentPadding),
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

@Composable
private fun GlassCardSample() {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    Column(
        modifier = Modifier
            .background(colors.background)
            .padding(spacing.space6),
        verticalArrangement = Arrangement.spacedBy(spacing.space4)
    ) {
        GlassLevel.entries.forEach { level ->
            GlassCard(modifier = Modifier.fillMaxWidth(), level = level) {
                Text(
                    text = stringResource(R.string.overlay_title),
                    style = SegundoAireTheme.typography.titleLg,
                    color = colors.onBackground
                )
                Text(
                    text = stringResource(R.string.overlay_subtitle),
                    style = SegundoAireTheme.typography.bodyMd,
                    color = colors.onBackground
                )
            }
        }
        GlassCard(modifier = Modifier.fillMaxWidth(), onClick = {}) {
            Text(
                text = stringResource(R.string.onboarding_continue),
                style = SegundoAireTheme.typography.titleMd,
                color = colors.onBackground
            )
        }
    }
}

@Preview(name = "GlassCard · claro")
@Composable
private fun GlassCardLightPreview() {
    SegundoAireTheme(darkTheme = false) { GlassCardSample() }
}

@Preview(name = "GlassCard · oscuro")
@Composable
private fun GlassCardDarkPreview() {
    SegundoAireTheme(darkTheme = true) { GlassCardSample() }
}
