package com.segundoaire.presentation.progress

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segundoaire.R
import com.segundoaire.domain.model.DayMinutes
import com.segundoaire.presentation.designsystem.components.ActionChip
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireColors
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.main.ScreenHeader
import com.segundoaire.presentation.panel.durationText
import com.segundoaire.presentation.panel.hourText
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// Corte del tronco de la referencia: 260 × 230 con la elipse de madera de 124 × 108
private val TrunkWidth = 260.dp
private val TrunkHeight = 230.dp
internal const val TrunkCutWidth = 260f
private val TrunkCenter = Offset(130f, 115f)
private const val WoodRadiusX = 124f
private const val WoodRadiusY = 108f
private const val RingRatio = 0.87f
private const val SelectedRingWidth = 5f
private const val LeafRingAlpha = 0.7f
private const val TrunkRingAlpha = 0.35f
private const val PithRadius = 5f
private const val SelectionMillis = 300

private val WeekHeight = 150.dp
private val FlowerSize = 16.dp
private val StemWidth = 6.dp
private val StemWidthSelected = 10.dp
private const val StemAlpha = 0.6f
private const val FutureAlpha = 0.5f
private val PetalCenters = arrayOf(Offset(0f, -5f), Offset(4.8f, -1.5f), Offset(3f, 4f), Offset(-3f, 4f), Offset(-4.8f, -1.5f))

/**
 * Progreso: el corte del tronco con un anillo por día (grosor según los minutos; los días
 * con meta en `leaf` y el resto en `trunk`), la semana en siete tallos con una flor si se
 * cumplieron 30 min y las cuatro cifras de la semana. Tocar un día ilumina su anillo en
 * `glow` y actualiza el título. Sin estado propio: [ProgressRoute] lo conecta.
 */
@Composable
fun ProgressScreen(
    state: ProgressUiState,
    onDaySelected: (LocalDate) -> Unit,
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
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(
            eyebrow = stringResource(R.string.nav_progress),
            title = stringResource(R.string.progress_heading),
            modifier = Modifier.padding(start = spacing.space5, end = spacing.space5, top = spacing.space4)
        )
        if (state.isDemo) {
            ActionChip(
                text = stringResource(R.string.progress_demo_chip),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = spacing.space4, top = spacing.space2)
            )
        }
        TrunkCut(rings = state.rings, selectedDay = state.selectedDay, modifier = Modifier.padding(top = spacing.space4))
        SelectedDayTitle(state = state, modifier = Modifier.padding(top = spacing.space1))
        WeekCard(
            week = state.week,
            today = state.today,
            selectedDay = state.selectedDay,
            onDaySelected = onDaySelected,
            modifier = Modifier.padding(start = spacing.space4, end = spacing.space4, top = 18.dp)
        )
        StatsGrid(state = state, modifier = Modifier.padding(start = spacing.space4, end = spacing.space4, top = spacing.space2))
    }
}

@Composable
private fun TrunkCut(rings: List<DayMinutes>, selectedDay: LocalDate?, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val selection = rings.map { ring ->
        animateFloatAsState(if (ring.date == selectedDay) 1f else 0f, motion.fade(SelectionMillis), label = "ring")
    }
    Canvas(
        modifier
            .size(TrunkWidth, TrunkHeight)
            .clearAndSetSemantics { }
    ) {
        scale(density, Offset.Zero) { drawTrunkCut(rings, colors) { selection[it].value } }
    }
}

/**
 * El corte del tronco en coordenadas de la referencia (260 × 230): la madera, un anillo por
 * día (del más antiguo, en el centro, a hoy) y la médula. [selection] da cuánto está
 * iluminado (0–1) el anillo [Int]. Lo usan Progreso y la imagen para compartir.
 */
internal fun DrawScope.drawTrunkCut(
    rings: List<DayMinutes>,
    colors: SegundoAireColors,
    selection: (Int) -> Float = { 0f }
) {
    drawOval(colors.wood, Offset(TrunkCenter.x - WoodRadiusX, TrunkCenter.y - WoodRadiusY), Size(WoodRadiusX * 2, WoodRadiusY * 2))
    rings.forEachIndexed { i, ring ->
        val rx = ProgressRules.ringRadiusX(i)
        val ry = rx * RingRatio
        val wobble = ProgressRules.ringWobble(i).toFloat()
        val goal = ProgressRules.hasFlower(ring.minutes)
        val base = if (goal) colors.leaf.copy(alpha = LeafRingAlpha) else colors.trunk.copy(alpha = TrunkRingAlpha)
        val s = selection(i)
        val width = ProgressRules.ringWidth(ring.minutes)
        drawOval(
            color = lerp(base, colors.glow, s),
            topLeft = Offset(TrunkCenter.x + wobble - rx, TrunkCenter.y - wobble - ry),
            size = Size(rx * 2, ry * 2),
            style = Stroke(width + (SelectedRingWidth - width) * s)
        )
    }
    drawCircle(colors.trunk, PithRadius, TrunkCenter)
}

@Composable
private fun SelectedDayTitle(state: ProgressUiState, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val day = state.selectedDay ?: return
    val minutes = state.selectedMinutes ?: 0
    val label = if (day == state.today) stringResource(R.string.progress_today) else dayTitle(day)
    Column(
        modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.progress_day_title, label, minutes),
            style = typography.serif(size = 22, lineHeight = 28).copy(fontFeatureSettings = "tnum"),
            color = colors.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(if (ProgressRules.hasFlower(minutes)) R.string.progress_day_flower else R.string.progress_day_thin),
            style = typography.bodyMd.copy(fontSize = 13.sp),
            color = colors.onBackgroundMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WeekCard(
    week: List<WeekDay>,
    today: LocalDate?,
    selectedDay: LocalDate?,
    onDaySelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        level = GlassLevel.Level2,
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 12.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.progress_week_label),
                style = typography.labelLg.copy(fontWeight = FontWeight.W700),
                color = colors.onBackground,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.progress_goal, ProgressRules.GOAL_MINUTES),
                style = typography.labelMd.copy(fontWeight = FontWeight.W400),
                color = colors.onBackgroundMuted
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = SegundoAireTheme.spacing.space2)
                .height(WeekHeight)
                .selectableGroup(),
            verticalAlignment = Alignment.Bottom
        ) {
            week.forEach { day ->
                DayStem(
                    day = day,
                    selected = day.date == selectedDay,
                    isToday = day.date == today,
                    onClick = { onDaySelected(day.date) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DayStem(day: WeekDay, selected: Boolean, isToday: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val locale = currentLocale()
    val minutes = day.minutes ?: 0
    val height by animateFloatAsState(
        if (day.isFuture) 6f else ProgressRules.stemHeight(minutes).toFloat(),
        motion.toggle(),
        label = "stemHeight"
    )
    val width by animateFloatAsState(
        if (selected) StemWidthSelected.value else StemWidth.value,
        motion.fade(SelectionMillis),
        label = "stemWidth"
    )
    val stemColor by animateColorAsState(
        if (selected) colors.glow else colors.trunk.copy(alpha = StemAlpha),
        motion.fade(SelectionMillis),
        label = "stemColor"
    )
    val name = if (isToday) stringResource(R.string.progress_today) else dayTitle(day.date)
    val description = if (day.isFuture) {
        stringResource(R.string.progress_day_future, name)
    } else {
        stringResource(R.string.progress_day_description, name, minutes)
    }
    val muted = colors.onBackgroundMuted
    Column(
        modifier
            .height(WeekHeight)
            .selectable(
                selected = selected,
                enabled = !day.isFuture,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.RadioButton,
                onClick = onClick
            )
            .clearAndSetSemantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Bottom)
    ) {
        DayFlower(visible = !day.isFuture && ProgressRules.hasFlower(minutes))
        Box(
            Modifier
                .width(width.dp)
                .height(height.coerceAtLeast(6f).dp)
                .graphicsLayer { alpha = if (day.isFuture) FutureAlpha else 1f }
                .drawBehind {
                    val radius = CornerRadius(6.dp.toPx())
                    if (day.isFuture) {
                        val stroke = 1.5.dp.toPx()
                        drawRoundRect(
                            color = muted,
                            topLeft = Offset(stroke / 2, stroke / 2),
                            size = Size(size.width - stroke, size.height - stroke),
                            cornerRadius = radius,
                            style = Stroke(stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.dp.toPx())))
                        )
                    } else {
                        drawRoundRect(stemColor, cornerRadius = radius)
                    }
                }
        )
        Text(
            text = day.date.dayOfWeek.getDisplayName(TextStyle.NARROW, locale),
            style = SegundoAireTheme.typography.labelMd.copy(fontWeight = if (selected) FontWeight.W700 else FontWeight.W500),
            color = if (selected) colors.onBackground else colors.onBackgroundMuted
        )
    }
}

@Composable
private fun DayFlower(visible: Boolean) {
    val colors = SegundoAireTheme.colors
    Canvas(Modifier.size(FlowerSize).graphicsLayer { alpha = if (visible) 1f else 0f }) {
        // Coordenadas del SVG de −10 a 10
        scale(size.width / 20f, Offset.Zero) {
            val center = Offset(10f, 10f)
            PetalCenters.forEach { drawCircle(colors.petal, 4f, center + it) }
            drawCircle(colors.gold, 2.5f, center)
        }
    }
}

@Composable
private fun StatsGrid(state: ProgressUiState, modifier: Modifier = Modifier) {
    val spacing = SegundoAireTheme.spacing
    val stats = listOf(
        durationText(state.weekMinutes) to stringResource(R.string.progress_stat_week_minutes),
        state.weekOpenings.toString() to pluralStringResource(R.plurals.panel_openings_label, state.weekOpenings),
        pluralStringResource(R.plurals.progress_streak_days, state.bestStreak, state.bestStreak) to stringResource(R.string.progress_stat_streak),
        (state.hardestHour?.let { hourText(it) } ?: stringResource(R.string.progress_stat_no_data)) to stringResource(R.string.progress_stat_hardest)
    )
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
        stats.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
                row.forEach { (value, label) -> StatCard(value, label, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    GlassCard(level = GlassLevel.Level2, modifier = modifier.semantics(mergeDescendants = true) { }) {
        Text(
            text = value,
            style = typography.serif(size = 26, lineHeight = 32).copy(fontFeatureSettings = "tnum"),
            color = colors.onBackground
        )
        Spacer(Modifier.height(2.dp))
        Text(text = label, style = typography.labelMd.copy(fontWeight = FontWeight.W400), color = colors.onBackgroundMuted)
    }
}

@Composable
private fun currentLocale(): Locale = LocalConfiguration.current.locales[0]

/** "Jue 15": día abreviado con mayúscula, sin el punto de la abreviatura, y el número. */
@Composable
private fun dayTitle(date: LocalDate): String {
    val locale = currentLocale()
    val short = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).trimEnd('.')
    return short.replaceFirstChar { it.titlecase(locale) } + " " + date.dayOfMonth
}

private val PreviewToday = LocalDate.of(2026, 10, 16)
private val PreviewMinutes = listOf(22, 35, 12, 40, 28, 15, 38, 32, 18, 45, 52, 47)

private val PreviewState = ProgressUiState(
    isLoading = false,
    today = PreviewToday,
    rings = PreviewMinutes.mapIndexed { i, m -> DayMinutes(PreviewToday.minusDays((PreviewMinutes.size - 1 - i).toLong()), m) },
    week = (0..6).map { i ->
        val date = PreviewToday.minusDays(4).plusDays(i.toLong())
        WeekDay(date, if (date.isAfter(PreviewToday)) null else PreviewMinutes[PreviewMinutes.size - 5 + i])
    },
    selectedDay = PreviewToday.minusDays(1),
    weekMinutes = 194,
    weekOpenings = 41,
    bestStreak = 5,
    hardestHour = 21
)

@Composable
private fun ProgressScreenSample() {
    ProgressScreen(state = PreviewState, onDaySelected = {}, modifier = Modifier.background(SegundoAireTheme.colors.background))
}

@Preview(name = "Progreso · claro", showSystemUi = true)
@Composable
private fun ProgressScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) { ProgressScreenSample() }
}

@Preview(name = "Progreso · oscuro", showSystemUi = true)
@Composable
private fun ProgressScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) { ProgressScreenSample() }
}
