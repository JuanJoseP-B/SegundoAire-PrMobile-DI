package com.segundoaire.presentation.onboarding.steps

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.pressScale
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass
import com.segundoaire.presentation.onboarding.OnboardingIcons
import com.segundoaire.presentation.onboarding.StepButton
import com.segundoaire.presentation.onboarding.StepContentSide
import com.segundoaire.presentation.onboarding.StepHeader
import com.segundoaire.presentation.onboarding.SvgIcon

/** Permiso que pide el onboarding. [required] marca los dos sin los que no hay pausa. */
internal enum class OnboardingPermission(
    @StringRes val title: Int,
    @StringRes val description: Int,
    val icon: String,
    val required: Boolean
) {
    Accessibility(
        R.string.onb_perm_accessibility_title, R.string.onb_perm_accessibility_desc,
        OnboardingIcons.Chart, required = true
    ),
    Overlay(
        R.string.onb_perm_overlay_title, R.string.onb_perm_overlay_desc,
        OnboardingIcons.Window, required = true
    ),
    Notifications(
        R.string.onb_perm_notifications_title, R.string.onb_perm_notifications_desc,
        OnboardingIcons.Bell, required = false
    )
}

@Composable
internal fun PermissionsText(step: Int, total: Int) {
    StepHeader(
        eyebrow = stringResource(R.string.onb_step_of, step, total),
        title = stringResource(R.string.onb_perms_title),
        subtitle = stringResource(R.string.onb_perms_subtitle)
    )
}

/** Tres filas de vidrio; "Activar" abre el ajuste y, al concederlo, la fila pasa a `success`. */
@Composable
internal fun PermissionsContent(
    granted: Map<OnboardingPermission, Boolean>,
    onEnable: (OnboardingPermission) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        Modifier
            .offset(x = StepContentSide, y = 262.dp)
            .width(358.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OnboardingPermission.entries.forEach { permission ->
            PermissionRow(
                permission = permission,
                granted = granted[permission] == true,
                onEnable = { onEnable(permission) }
            )
        }
    }
    StepButton(
        text = stringResource(R.string.onboarding_continue),
        onClick = onContinue,
        enabled = OnboardingPermission.entries.filter { it.required }.all { granted[it] == true }
    )
}

@Composable
private fun PermissionRow(permission: OnboardingPermission, granted: Boolean, onEnable: () -> Unit) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val motion = SegundoAireTheme.motion

    val tile = animateColorAsState(
        targetValue = if (granted) colors.successContainer else colors.warningContainer,
        animationSpec = motion.fade(500),
        label = "permissionTile"
    )
    val pop = remember { Animatable(1f) }
    LaunchedEffect(granted) {
        if (granted && !motion.reduced) {
            pop.snapTo(0.6f)
            pop.animateTo(1f, motion.bloom(true, 420))
        }
    }

    Row(
        Modifier
            .glass(GlassLevel.Level2, SegundoAireTheme.shapes.radiusMd)
            .padding(start = 16.dp, top = 14.dp, end = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier
                .size(40.dp)
                .background(tile.value, SegundoAireTheme.shapes.radiusSm)
                .graphicsLayer {
                    scaleX = pop.value
                    scaleY = pop.value
                },
            contentAlignment = Alignment.Center
        ) {
            SvgIcon(
                path = if (granted) OnboardingIcons.Check else permission.icon,
                color = if (granted) colors.success else colors.warning,
                size = 20.dp
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(permission.title),
                style = typography.titleMd.copy(fontSize = 15.sp),
                color = colors.onBackground
            )
            Text(
                text = stringResource(permission.description),
                style = typography.bodyMd.copy(fontSize = 13.sp, lineHeight = 18.sp),
                color = colors.onBackgroundMuted
            )
        }
        if (granted) {
            Text(
                text = stringResource(R.string.onb_perm_granted),
                style = typography.labelLg.copy(fontSize = 13.sp),
                color = colors.success
            )
        } else {
            SmallButton(text = stringResource(R.string.onb_perm_enable), onClick = onEnable)
        }
    }
}

/** Botón pequeño del prototipo: píldora de 40 dp dentro de un área táctil de 48 dp. */
@Composable
internal fun SmallButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val source = remember { MutableInteractionSource() }
    Box(
        modifier
            .defaultMinSize(minHeight = 48.dp)
            .pressScale(source)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .height(40.dp)
                .background(colors.primary, SegundoAireTheme.shapes.radiusFull)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = SegundoAireTheme.typography.labelLg.copy(fontSize = 13.sp, fontWeight = FontWeight.W600),
                color = colors.onPrimary
            )
        }
    }
}
