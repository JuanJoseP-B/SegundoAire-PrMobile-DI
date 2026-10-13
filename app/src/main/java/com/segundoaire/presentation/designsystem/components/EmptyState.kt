package com.segundoaire.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass

private val EmptyStateIconSize = 24.dp

/**
 * Estado vacío de una lista o sección: qué falta y, si aplica, la acción para
 * resolverlo. Va sobre `background`, por eso el mensaje puede usar el tono muted.
 */
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    Column(
        modifier = modifier.padding(spacing.space6),
        verticalArrangement = Arrangement.spacedBy(spacing.space2),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .padding(bottom = spacing.space2)
                    .size(spacing.space16)
                    .glass(GlassLevel.Level1, SegundoAireTheme.shapes.radiusFull),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(EmptyStateIconSize)
                )
            }
        }
        Text(
            text = title,
            style = SegundoAireTheme.typography.titleLg,
            color = colors.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            style = SegundoAireTheme.typography.bodyMd,
            color = colors.onBackgroundMuted,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            SecondaryButton(
                text = actionLabel,
                onClick = onAction,
                modifier = Modifier.padding(top = spacing.space3)
            )
        }
    }
}

@Composable
private fun EmptyStateSample() {
    Box(Modifier.background(SegundoAireTheme.colors.background)) {
        EmptyState(
            title = stringResource(R.string.dashboard_apps_title),
            message = stringResource(R.string.dashboard_empty),
            modifier = Modifier.fillMaxWidth(),
            icon = SegundoAireIcons.Device,
            actionLabel = stringResource(R.string.onboarding_continue),
            onAction = {}
        )
    }
}

@Preview(name = "EmptyState · claro")
@Composable
private fun EmptyStateLightPreview() {
    SegundoAireTheme(darkTheme = false) { EmptyStateSample() }
}

@Preview(name = "EmptyState · oscuro")
@Composable
private fun EmptyStateDarkPreview() {
    SegundoAireTheme(darkTheme = true) { EmptyStateSample() }
}
