package com.segundoaire.presentation.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segundoaire.R
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.TreeState
import com.segundoaire.presentation.designsystem.components.ActionChip
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.components.PrimaryButton
import com.segundoaire.presentation.designsystem.components.Toggle
import com.segundoaire.presentation.designsystem.components.focusRing
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.GoalOptions
import com.segundoaire.presentation.onboarding.SvgIcon
import com.segundoaire.presentation.panel.MissingPermission
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val AvatarSize = 72.dp
private val RowHeight = 56.dp
private const val AvatarTrunk = "M36 64 C35 54 37 46 36 38"
private const val AccentSoftAlpha = 0.14f

/**
 * Perfil: avatar con un árbol pequeño, el nombre en Fraunces y la fecha en que se plantó;
 * las metas como chips con "Editar"; los ajustes en vidrio (plan y modo, sonidos y
 * vibración, permisos y cuenta) y "Compartir mi árbol". Sin estado propio: [ProfileRoute]
 * lo conecta.
 */
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onEditGoals: () -> Unit,
    onOpenPlan: () -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenAccount: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val spacing = SegundoAireTheme.spacing
    val direction = LocalLayoutDirection.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = contentPadding.calculateStartPadding(direction),
                end = contentPadding.calculateEndPadding(direction),
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding() + spacing.space6
            )
    ) {
        ProfileHeader(
            name = state.savedName.trim(),
            plantedAt = state.tree.plantedAt,
            modifier = Modifier.padding(start = spacing.space5, end = spacing.space5, top = spacing.space4)
        )
        Eyebrow(stringResource(R.string.profile_goals_eyebrow), Modifier.padding(start = spacing.space5, top = spacing.space6, bottom = spacing.space2))
        GoalChips(goals = state.tree.goals, onEdit = onEditGoals, modifier = Modifier.padding(horizontal = spacing.space4))
        Eyebrow(stringResource(R.string.settings_title), Modifier.padding(start = spacing.space5, top = spacing.space6, bottom = spacing.space2))
        SettingsCard(
            state = state,
            onOpenPlan = onOpenPlan,
            onSoundChange = onSoundChange,
            onOpenPermissions = onOpenPermissions,
            onOpenAccount = onOpenAccount,
            modifier = Modifier.padding(horizontal = spacing.space4)
        )
        PrimaryButton(
            text = stringResource(R.string.profile_share),
            onClick = onShare,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = spacing.space4, end = spacing.space4, top = spacing.space5),
            icon = SegundoAireIcons.Share
        )
    }
}

@Composable
private fun ProfileHeader(name: String, plantedAt: LocalDate?, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val locale = LocalConfiguration.current.locales[0]
    Row(modifier.fillMaxWidth().semantics(mergeDescendants = true) { heading() }, verticalAlignment = Alignment.CenterVertically) {
        TreeAvatar()
        Spacer(Modifier.width(SegundoAireTheme.spacing.space4))
        Column {
            if (name.isNotEmpty()) {
                Text(
                    text = name,
                    style = typography.serif(size = 30, lineHeight = 36, tracking = typography.serifTitle.letterSpacing),
                    color = colors.onBackground
                )
            }
            if (plantedAt != null) {
                Text(
                    text = stringResource(
                        R.string.profile_planted,
                        plantedAt.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
                    ),
                    style = typography.bodyMd.copy(fontSize = 13.sp),
                    color = colors.onBackgroundMuted
                )
            }
        }
    }
}

/** Avatar de 72 dp: un árbol pequeño de tres copas sobre un círculo de `glow` al 14 %. */
@Composable
private fun TreeAvatar() {
    val colors = SegundoAireTheme.colors
    val trunk = remember { PathParser().parsePathString(AvatarTrunk).toPath() }
    Canvas(Modifier.size(AvatarSize).clearAndSetSemantics { }) {
        scale(density, Offset.Zero) {
            drawCircle(colors.glow.copy(alpha = AccentSoftAlpha), 36f, Offset(36f, 36f))
            drawPath(trunk, colors.trunk, style = Stroke(3f, cap = StrokeCap.Round))
            drawCircle(colors.leaf.copy(alpha = 0.7f), 15f, Offset(36f, 28f))
            drawCircle(colors.trunk.copy(alpha = 0.55f), 11f, Offset(25f, 36f))
            drawCircle(colors.leaf.copy(alpha = 0.45f), 11f, Offset(47f, 36f))
        }
    }
}

@Composable
private fun Eyebrow(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.toUpperCase(Locale.current),
        style = SegundoAireTheme.typography.labelSmCaps.copy(fontWeight = FontWeight.W600),
        color = SegundoAireTheme.colors.onBackgroundMuted,
        modifier = modifier.semantics { heading() }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoalChips(goals: List<Goal>, onEdit: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val editDescription = stringResource(R.string.profile_goals_edit_description)
    FlowRow(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SegundoAireTheme.spacing.space2)) {
        goals.forEach { goal ->
            val option = GoalOptions.firstOrNull { it.goal == goal } ?: return@forEach
            ActionChip(
                text = stringResource(option.label),
                modifier = Modifier.padding(vertical = 4.dp),
                leading = { SvgIcon(path = option.icon, color = colors.onBackground, size = 14.dp) }
            )
        }
        ActionChip(
            text = stringResource(R.string.profile_goals_edit),
            onClick = onEdit,
            dashed = true,
            modifier = Modifier.semantics { contentDescription = editDescription }
        )
    }
}

@Composable
private fun SettingsCard(
    state: ProfileUiState,
    onOpenPlan: () -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    GlassCard(modifier = modifier.fillMaxWidth(), level = GlassLevel.Level2, contentPadding = PaddingValues(vertical = 4.dp)) {
        SettingsRow(
            title = stringResource(R.string.profile_row_plan),
            value = stringResource(if (state.rigor == RigorLevel.STRICT) R.string.rigor_strict else R.string.rigor_mindful),
            onClick = onOpenPlan
        )
        Divider()
        SoundRow(enabled = state.soundEnabled, onChange = onSoundChange)
        Divider()
        val granted = state.missingPermission == null
        SettingsRow(
            title = stringResource(R.string.profile_row_permissions),
            value = stringResource(if (granted) R.string.profile_permissions_ok else R.string.profile_permissions_missing),
            valueColor = if (granted) colors.success else colors.warning,
            onClick = onOpenPermissions
        )
        Divider()
        SettingsRow(title = stringResource(R.string.profile_row_account), onClick = onOpenAccount)
    }
}

@Composable
private fun SettingsRow(
    title: String,
    onClick: () -> Unit,
    value: String? = null,
    valueColor: Color = SegundoAireTheme.colors.onBackgroundMuted
) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val source = remember { MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = RowHeight)
            .focusRing(source, SegundoAireTheme.shapes.radiusMd)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = SegundoAireTheme.spacing.space4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = typography.labelLg, color = colors.onBackground, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(
                text = value,
                style = typography.bodyMd.copy(fontSize = 13.sp, fontWeight = FontWeight.W600),
                color = valueColor,
                modifier = Modifier.padding(end = SegundoAireTheme.spacing.space2)
            )
        }
        Icon(SegundoAireIcons.ChevronRight, contentDescription = null, tint = colors.onBackground, modifier = Modifier.size(18.dp))
    }
}

/** "Sonidos y vibración": toda la fila es el interruptor. */
@Composable
private fun SoundRow(enabled: Boolean, onChange: (Boolean) -> Unit) {
    val source = remember { MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = RowHeight)
            .focusRing(source, SegundoAireTheme.shapes.radiusMd)
            .toggleable(value = enabled, interactionSource = source, indication = null, role = Role.Switch, onValueChange = onChange)
            .padding(horizontal = SegundoAireTheme.spacing.space4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.profile_row_sound),
            style = SegundoAireTheme.typography.labelLg,
            color = SegundoAireTheme.colors.onBackground,
            modifier = Modifier.weight(1f)
        )
        Toggle(checked = enabled)
    }
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = SegundoAireTheme.spacing.space4)
            .height(1.dp)
            .background(SegundoAireTheme.colors.outlineVariant)
    )
}

@Composable
private fun ProfileScreenSample(state: ProfileUiState) {
    ProfileScreen(
        state = state,
        onEditGoals = {},
        onOpenPlan = {},
        onSoundChange = {},
        onOpenPermissions = {},
        onOpenAccount = {},
        onShare = {},
        modifier = Modifier.background(SegundoAireTheme.colors.background)
    )
}

private val PreviewProfile = ProfileUiState(
    isLoading = false,
    savedName = "Juan",
    tree = TreeState(goals = listOf(Goal.FREE_TIME, Goal.SLEEP), plantedAt = LocalDate.of(2026, 10, 16))
)

@Preview(name = "Perfil · claro", showSystemUi = true)
@Composable
private fun ProfileScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) { ProfileScreenSample(PreviewProfile) }
}

@Preview(name = "Perfil · oscuro, permisos pendientes", showSystemUi = true)
@Composable
private fun ProfileScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        ProfileScreenSample(PreviewProfile.copy(rigor = RigorLevel.STRICT, soundEnabled = false, missingPermission = MissingPermission.OVERLAY))
    }
}
