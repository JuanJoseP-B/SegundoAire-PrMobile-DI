package com.segundoaire.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.TextButton
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

private val ConfirmMinHeight = 48.dp

/**
 * Confirmación de "Reiniciar app". El botón destructivo va sobre `errorContainer`
 * con texto `error`, nunca en rojo puro, y la acción segura es la que recibe el
 * foco natural: cancelar queda a la izquierda del botón destructivo.
 */
@Composable
fun ResetConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        confirmButton = { DestructiveButton(text = stringResource(R.string.settings_reset_confirm), onClick = onConfirm) },
        dismissButton = {
            TextButton(
                text = stringResource(R.string.settings_reset_cancel),
                onClick = onDismiss,
                color = colors.onBackground
            )
        },
        title = {
            Text(text = stringResource(R.string.settings_reset_dialog_title), style = typography.titleLg)
        },
        text = {
            Text(text = stringResource(R.string.settings_reset_dialog_message), style = typography.bodyLg)
        },
        shape = SegundoAireTheme.shapes.radiusLg,
        containerColor = colors.background,
        titleContentColor = colors.onBackground,
        textContentColor = colors.onBackground
    )
}

@Composable
private fun DestructiveButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val shape = SegundoAireTheme.shapes.radiusFull
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = ConfirmMinHeight)
            .clip(shape)
            .background(colors.errorContainer)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = spacing.space5, vertical = spacing.space3),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = SegundoAireTheme.typography.labelLg, color = colors.error)
    }
}

@Preview(name = "ResetConfirmDialog · claro")
@Composable
private fun ResetConfirmDialogLightPreview() {
    SegundoAireTheme(darkTheme = false) { ResetConfirmDialog(onConfirm = {}, onDismiss = {}) }
}

@Preview(name = "ResetConfirmDialog · oscuro")
@Composable
private fun ResetConfirmDialogDarkPreview() {
    SegundoAireTheme(darkTheme = true) { ResetConfirmDialog(onConfirm = {}, onDismiss = {}) }
}
