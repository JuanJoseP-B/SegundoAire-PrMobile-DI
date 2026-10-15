package com.segundoaire.presentation.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.PrimaryButton
import com.segundoaire.presentation.designsystem.components.QuestionScaffold
import com.segundoaire.presentation.designsystem.components.TextButton
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/**
 * Permisos guiados: accesibilidad y mostrar sobre otras apps. "Continuar" exige
 * ambos; "Hacerlo después" deja pasar sin ellos y el Panel avisa con su banner.
 */
@Composable
fun PermissionsScreen(
    state: PermissionsUiState,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = SegundoAireTheme.spacing

    QuestionScaffold(
        action = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing.space2),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryButton(
                    text = stringResource(R.string.setup_permissions_continue),
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canContinue
                )
                TextButton(
                    text = stringResource(R.string.setup_permissions_later),
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        modifier = modifier,
        title = stringResource(R.string.setup_permissions_title),
        subtitle = stringResource(R.string.setup_permissions_subtitle)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
            PermissionChecklistItem(
                title = stringResource(R.string.setup_permission_accessibility_title),
                benefit = stringResource(R.string.setup_permission_accessibility_benefit),
                isGranted = state.isAccessibilityGranted,
                onOpenSettings = onOpenAccessibilitySettings,
                modifier = Modifier.fillMaxWidth()
            )
            PermissionChecklistItem(
                title = stringResource(R.string.setup_permission_overlay_title),
                benefit = stringResource(R.string.setup_permission_overlay_benefit),
                isGranted = state.isOverlayGranted,
                onOpenSettings = onOpenOverlaySettings,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(name = "Permisos · claro")
@Composable
private fun PermissionsScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) {
        PermissionsScreen(
            state = PermissionsUiState(isAccessibilityGranted = true),
            onOpenAccessibilitySettings = {},
            onOpenOverlaySettings = {},
            onContinue = {},
            onSkip = {}
        )
    }
}

@Preview(name = "Permisos · oscuro")
@Composable
private fun PermissionsScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        PermissionsScreen(
            state = PermissionsUiState(isAccessibilityGranted = true, isOverlayGranted = true),
            onOpenAccessibilitySettings = {},
            onOpenOverlaySettings = {},
            onContinue = {},
            onSkip = {}
        )
    }
}
