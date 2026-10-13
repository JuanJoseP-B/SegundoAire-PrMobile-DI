package com.segundoaire.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

private val BannerIconSize = 24.dp

/**
 * Aviso de un permiso pendiente, sobre `warningContainer`. Es una advertencia,
 * no un error: lleva ícono y texto, y una sola acción para resolverlo.
 */
@Composable
fun PermissionBanner(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    Column(
        modifier = modifier
            .clip(SegundoAireTheme.shapes.radiusMd)
            .background(colors.warningContainer)
            .padding(start = spacing.space4, top = spacing.space4, end = spacing.space2, bottom = spacing.space2)
    ) {
        Row(
            modifier = Modifier.padding(end = spacing.space2),
            horizontalArrangement = Arrangement.spacedBy(spacing.space3)
        ) {
            Icon(
                imageVector = SegundoAireIcons.Warning,
                contentDescription = null,
                tint = colors.warning,
                modifier = Modifier.size(BannerIconSize)
            )
            Column(verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
                Text(text = title, style = SegundoAireTheme.typography.titleMd, color = colors.onBackground)
                Text(text = message, style = SegundoAireTheme.typography.bodyMd, color = colors.onBackground)
            }
        }
        TextButton(
            text = actionLabel,
            onClick = onAction,
            modifier = Modifier.align(Alignment.End),
            color = colors.warning
        )
    }
}

@Composable
private fun PermissionBannerSample() {
    Box(
        Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(SegundoAireTheme.spacing.space6)
    ) {
        PermissionBanner(
            title = stringResource(R.string.banner_title),
            message = stringResource(R.string.banner_overlay_disabled),
            actionLabel = stringResource(R.string.banner_action),
            onAction = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "PermissionBanner · claro")
@Composable
private fun PermissionBannerLightPreview() {
    SegundoAireTheme(darkTheme = false) { PermissionBannerSample() }
}

@Preview(name = "PermissionBanner · oscuro")
@Composable
private fun PermissionBannerDarkPreview() {
    SegundoAireTheme(darkTheme = true) { PermissionBannerSample() }
}
