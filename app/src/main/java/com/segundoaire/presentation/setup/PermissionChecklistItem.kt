package com.segundoaire.presentation.setup

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.components.SecondaryButton
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

private val MarkSize = 32.dp
private val MarkIconSize = 20.dp
private val MarkBorderWidth = 1.5.dp

/**
 * Un permiso del checklist: qué es, su beneficio en una línea y, mientras
 * falta, el botón que abre su ajuste. Al concederse, la marca se rellena con
 * `Motion.toggle` y el botón da paso a la palabra "Permitido" (el color solo
 * nunca comunica el estado, §3.2).
 */
@Composable
internal fun PermissionChecklistItem(
    title: String,
    benefit: String,
    isGranted: Boolean,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val typography = SegundoAireTheme.typography

    GlassCard(modifier = modifier, level = GlassLevel.Level1) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.space3)
        ) {
            GrantedMark(isGranted)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.space1)
            ) {
                Text(text = title, style = typography.titleMd, color = colors.onBackground)
                Text(text = benefit, style = typography.bodyMd, color = colors.onBackgroundMuted)
                if (isGranted) {
                    Text(
                        text = stringResource(R.string.setup_permission_granted),
                        style = typography.labelMd,
                        color = colors.onBackground,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    )
                } else {
                    SecondaryButton(
                        text = stringResource(R.string.setup_permission_open_settings),
                        onClick = onOpenSettings,
                        modifier = Modifier.padding(top = spacing.space2)
                    )
                }
            }
        }
    }
}

/**
 * Círculo con contorno que se rellena de `successContainer` y muestra el check
 * al concederse. Con reducir movimiento no hay escala: solo se funde.
 */
@Composable
private fun GrantedMark(isGranted: Boolean) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val fill by animateColorAsState(
        targetValue = if (isGranted) colors.successContainer else colors.successContainer.copy(alpha = 0f),
        animationSpec = motion.calm(),
        label = "permissionMarkFill"
    )
    val border by animateColorAsState(
        targetValue = if (isGranted) colors.success else colors.outline,
        animationSpec = motion.calm(),
        label = "permissionMarkBorder"
    )
    // El resorte rebota: la opacidad se acota, la escala no
    val check by animateFloatAsState(
        targetValue = if (isGranted) 1f else 0f,
        animationSpec = motion.toggle(),
        label = "permissionMarkCheck"
    )

    Box(
        modifier = Modifier
            .size(MarkSize)
            .clip(CircleShape)
            .background(fill)
            .border(MarkBorderWidth, border, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = SegundoAireIcons.Check,
            contentDescription = null,
            tint = colors.success,
            modifier = Modifier
                .size(MarkIconSize)
                .graphicsLayer {
                    val scale = if (motion.reduced) 1f else check
                    scaleX = scale
                    scaleY = scale
                    alpha = check.coerceIn(0f, 1f)
                }
        )
    }
}

@Composable
private fun PermissionChecklistItemSample() {
    val spacing = SegundoAireTheme.spacing
    Column(
        modifier = Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(spacing.space6),
        verticalArrangement = Arrangement.spacedBy(spacing.space3)
    ) {
        PermissionChecklistItem(
            title = stringResource(R.string.setup_permission_accessibility_title),
            benefit = stringResource(R.string.setup_permission_accessibility_benefit),
            isGranted = true,
            onOpenSettings = {},
            modifier = Modifier.fillMaxWidth()
        )
        PermissionChecklistItem(
            title = stringResource(R.string.setup_permission_overlay_title),
            benefit = stringResource(R.string.setup_permission_overlay_benefit),
            isGranted = false,
            onOpenSettings = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "PermissionChecklistItem · claro")
@Composable
private fun PermissionChecklistItemLightPreview() {
    SegundoAireTheme(darkTheme = false) { PermissionChecklistItemSample() }
}

@Preview(name = "PermissionChecklistItem · oscuro")
@Composable
private fun PermissionChecklistItemDarkPreview() {
    SegundoAireTheme(darkTheme = true) { PermissionChecklistItemSample() }
}
