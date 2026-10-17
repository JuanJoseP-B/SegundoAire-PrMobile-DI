package com.segundoaire.presentation.blocks

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segundoaire.R
import com.segundoaire.domain.model.ActiveBlock
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.presentation.designsystem.components.AppIconPlaceholder
import com.segundoaire.presentation.designsystem.components.AppToggleRow
import com.segundoaire.presentation.designsystem.components.EmptyState
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.components.TextButton
import com.segundoaire.presentation.designsystem.components.focusRing
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.main.ScreenHeader

private val SegmentHeight = 44.dp
private val SegmentInset = 2.dp
private val SegmentTouchHeight = 48.dp
private val ModeBorder = 2.dp
private const val ColorFadeMillis = 250

/**
 * Bloques: la tarjeta "Iniciar bloque" (duración, modo y el botón que hay que mantener
 * 1,5 s) y las apps vigiladas con su toggle. Con un bloque en curso el botón dice
 * "Bloque en curso" y un toque abre el modo enfoque ([onOpenFocus]). Sin bloques
 * programados: solo se inicia uno ahora. Sin estado propio: [TimerRoute] lo conecta.
 */
@Composable
fun TimerScreen(
    state: TimerUiState,
    onDurationSelected: (minutes: Int) -> Unit,
    onRigorSelected: (RigorLevel) -> Unit,
    onStart: () -> Unit,
    onOpenFocus: () -> Unit,
    onToggleApp: (packageName: String, isBlocked: Boolean) -> Unit,
    onEditApps: () -> Unit,
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
        ScreenHeader(
            eyebrow = stringResource(R.string.nav_blocks),
            title = stringResource(R.string.blocks_title),
            modifier = Modifier.padding(start = spacing.space5, end = spacing.space5, top = spacing.space4)
        )
        StartBlockCard(
            state = state,
            onDurationSelected = onDurationSelected,
            onRigorSelected = onRigorSelected,
            onStart = onStart,
            onOpenFocus = onOpenFocus,
            modifier = Modifier.padding(start = spacing.space4, end = spacing.space4, top = spacing.space5)
        )
        AppsHeader(
            onEditApps = onEditApps,
            modifier = Modifier.padding(start = spacing.space5, end = spacing.space2, top = spacing.space5)
        )
        if (state.apps.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.panel_empty_title),
                message = stringResource(R.string.panel_empty_message),
                modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.space4),
                icon = SegundoAireIcons.Device,
                actionLabel = stringResource(R.string.panel_empty_action),
                onAction = onEditApps
            )
        } else {
            Column(
                modifier = Modifier.padding(horizontal = spacing.space4),
                verticalArrangement = Arrangement.spacedBy(spacing.space2)
            ) {
                state.apps.forEachIndexed { index, app ->
                    WatchedAppRow(app = app, index = index, onToggle = { onToggleApp(app.packageName, it) })
                }
            }
        }
    }
}

@Composable
private fun StartBlockCard(
    state: TimerUiState,
    onDurationSelected: (minutes: Int) -> Unit,
    onRigorSelected: (RigorLevel) -> Unit,
    onStart: () -> Unit,
    onOpenFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val typography = SegundoAireTheme.typography
    val running = state.isRunning

    GlassCard(modifier = modifier.fillMaxWidth(), level = GlassLevel.Level2, contentPadding = PaddingValues(spacing.space5)) {
        Text(
            text = stringResource(R.string.blocks_start_title),
            style = typography.titleMd.copy(fontSize = 17.sp, fontWeight = FontWeight.W700),
            color = colors.onBackground
        )
        Text(
            text = stringResource(R.string.blocks_start_body),
            style = typography.bodyMd.copy(fontSize = 13.sp),
            color = colors.onBackgroundMuted,
            modifier = Modifier.padding(top = 2.dp)
        )
        DurationSegmented(
            selectedMinutes = state.selectedMinutes,
            onSelected = onDurationSelected,
            enabled = !running,
            modifier = Modifier.padding(top = spacing.space4)
        )
        ModeCards(
            selected = state.rigor,
            onSelected = onRigorSelected,
            enabled = !running,
            modifier = Modifier.padding(top = spacing.space3)
        )
        HoldFillButton(
            text = stringResource(if (running) R.string.blocks_hold_running else R.string.blocks_hold),
            onCommit = onStart,
            container = colors.primary,
            content = colors.onPrimary,
            fill = colors.glow,
            modifier = Modifier.padding(top = spacing.space4),
            onTap = if (running) onOpenFocus else null,
            accessibilityLabel = stringResource(if (running) R.string.blocks_hold_running else R.string.blocks_hold_description)
        )
        Text(
            text = stringResource(R.string.blocks_hold_hint),
            style = typography.labelMd.copy(fontWeight = FontWeight.W400),
            color = colors.onBackgroundMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        )
    }
}

/**
 * Segmentado de 5 / 10 / 15 / 30 min: píldora de `glass1` con un indicador `primary` que
 * se desliza con `Motion.toggle`. Semántica de grupo de radio.
 */
@Composable
private fun DurationSegmented(
    selectedMinutes: Int,
    onSelected: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val options = ActiveBlock.DURATION_OPTIONS_MINUTES
    val selectedIndex = options.indexOf(selectedMinutes).coerceAtLeast(0)
    val position by animateFloatAsState(selectedIndex.toFloat(), motion.toggle(), label = "durationIndicator")

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(SegmentTouchHeight),
        contentAlignment = Alignment.Center
    ) {
        val segment = maxWidth / options.size
        Box(
            Modifier
                .fillMaxWidth()
                .height(SegmentHeight)
                .background(colors.glass1, CircleShape)
        ) {
            Box(
                Modifier
                    .offset(x = segment * position + SegmentInset)
                    .padding(vertical = 4.dp)
                    .width(segment - SegmentInset * 2)
                    .height(SegmentHeight - 8.dp)
                    .background(colors.primary, CircleShape)
            )
        }
        Row(Modifier.fillMaxWidth().selectableGroup()) {
            options.forEachIndexed { index, minutes ->
                val selected = index == selectedIndex
                val color by animateColorAsState(
                    if (selected) colors.onPrimary else colors.onBackground,
                    motion.fade(ColorFadeMillis),
                    label = "durationLabel"
                )
                val source = remember { MutableInteractionSource() }
                Box(
                    Modifier
                        .weight(1f)
                        .height(SegmentTouchHeight)
                        .focusRing(source, CircleShape)
                        .selectable(
                            selected = selected,
                            enabled = enabled,
                            interactionSource = source,
                            indication = null,
                            role = Role.RadioButton,
                            onClick = { onSelected(minutes) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.timer_duration_option, minutes),
                        style = SegundoAireTheme.typography.labelLg.copy(fontSize = 13.sp),
                        color = if (enabled) color else colors.disabledContent
                    )
                }
            }
        }
    }
}

/** Mindful o Strict: dos tarjetas; la activa lleva borde `primary` de 2 dp. */
@Composable
private fun ModeCards(
    selected: RigorLevel,
    onSelected: (RigorLevel) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val spacing = SegundoAireTheme.spacing
    Row(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(spacing.space2)
    ) {
        ModeCard(
            label = stringResource(R.string.rigor_mindful),
            description = stringResource(R.string.blocks_mindful_desc),
            selected = selected == RigorLevel.MINDFUL,
            enabled = enabled,
            onClick = { onSelected(RigorLevel.MINDFUL) },
            modifier = Modifier.weight(1f)
        )
        ModeCard(
            label = stringResource(R.string.rigor_strict),
            description = stringResource(R.string.blocks_strict_desc),
            selected = selected == RigorLevel.STRICT,
            enabled = enabled,
            onClick = { onSelected(RigorLevel.STRICT) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ModeCard(
    label: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val shape = SegundoAireTheme.shapes.radiusMd
    val source = remember { MutableInteractionSource() }
    val border by animateColorAsState(
        if (selected) colors.primary else colors.primary.copy(alpha = 0f),
        motion.fade(ColorFadeMillis),
        label = "modeBorder"
    )
    Column(
        modifier
            .heightIn(min = 48.dp)
            .focusRing(source, shape)
            .background(colors.glass1, shape)
            .border(ModeBorder, border, shape)
            .selectable(
                selected = selected,
                enabled = enabled,
                interactionSource = source,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = SegundoAireTheme.spacing.space3)
    ) {
        Text(
            text = label,
            style = SegundoAireTheme.typography.labelLg.copy(fontWeight = FontWeight.W700),
            color = if (enabled || selected) colors.onBackground else colors.disabledContent
        )
        Text(
            text = description,
            style = SegundoAireTheme.typography.labelMd.copy(fontWeight = FontWeight.W400),
            color = colors.onBackgroundMuted,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun AppsHeader(onEditApps: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val editDescription = stringResource(R.string.blocks_apps_edit_description)
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.blocks_apps_title),
            style = SegundoAireTheme.typography.titleMd.copy(fontSize = 17.sp, fontWeight = FontWeight.W700),
            color = colors.onBackground,
            modifier = Modifier.weight(1f)
        )
        TextButton(
            text = stringResource(R.string.blocks_apps_edit),
            onClick = onEditApps,
            color = colors.onBackground,
            modifier = Modifier.semantics { contentDescription = editDescription }
        )
    }
}

/** App vigilada: ícono genérico de color (sin logos), los minutos estimados de hoy y su toggle. */
@Composable
private fun WatchedAppRow(app: WatchedApp, index: Int, onToggle: (Boolean) -> Unit) {
    val minutes = app.minutesToday
    val supporting = if (minutes == null) {
        stringResource(R.string.blocks_app_no_data)
    } else {
        stringResource(R.string.blocks_app_minutes, minutes) + " " + stringResource(R.string.blocks_app_today)
    }
    AppToggleRow(
        name = app.name,
        checked = app.isBlocked,
        onCheckedChange = onToggle,
        modifier = Modifier.fillMaxWidth(),
        supportingText = supporting
    ) {
        AppIconPlaceholder(index = index)
    }
}

@Composable
private fun TimerScreenSample(state: TimerUiState) {
    TimerScreen(
        state = state,
        onDurationSelected = {},
        onRigorSelected = {},
        onStart = {},
        onOpenFocus = {},
        onToggleApp = { _, _ -> },
        onEditApps = {},
        modifier = Modifier.background(SegundoAireTheme.colors.background)
    )
}

private val PreviewApps = listOf(
    WatchedApp("a", "Videos", isBlocked = true, minutesToday = 48),
    WatchedApp("b", "Social", isBlocked = true, minutesToday = 31),
    WatchedApp("c", "Chat", isBlocked = true, minutesToday = 22),
    WatchedApp("d", "Juegos", isBlocked = false)
)

@Preview(name = "Bloques · claro", showSystemUi = true)
@Composable
private fun TimerScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) { TimerScreenSample(TimerUiState(apps = PreviewApps)) }
}

@Preview(name = "Bloques · oscuro", showSystemUi = true)
@Composable
private fun TimerScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) { TimerScreenSample(TimerUiState(apps = PreviewApps)) }
}

@Preview(name = "Bloques · en curso, Strict · oscuro", showSystemUi = true)
@Composable
private fun TimerScreenRunningDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        TimerScreenSample(TimerUiState(isRunning = true, rigor = RigorLevel.STRICT, remainingMillis = 600_000L, apps = PreviewApps))
    }
}
