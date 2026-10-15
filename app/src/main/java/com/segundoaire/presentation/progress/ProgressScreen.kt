package com.segundoaire.presentation.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.domain.model.DayMinutes
import com.segundoaire.presentation.designsystem.components.AnimatedCounter
import com.segundoaire.presentation.designsystem.components.EmptyState
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.components.Skeleton
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass
import com.segundoaire.presentation.onboarding.OnboardingEyebrow
import java.time.LocalDate

private val SkeletonImpactHeight = 96.dp
private val SkeletonChartHeight = 200.dp

/**
 * Progreso de la semana: el dato de impacto (minutos recuperados), las tarjetas
 * de aperturas evitadas y racha, y el gráfico día a día. Sin datos muestra un
 * estado vacío sin culpa; con el modo demo, un chip lo avisa. Sin estado propio:
 * [ProgressRoute] lo conecta.
 */
@Composable
fun ProgressScreen(
    state: ProgressUiState,
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
                start = spacing.space5 + contentPadding.calculateStartPadding(direction),
                top = spacing.space6 + contentPadding.calculateTopPadding(),
                end = spacing.space5 + contentPadding.calculateEndPadding(direction),
                bottom = spacing.space6 + contentPadding.calculateBottomPadding()
            ),
        verticalArrangement = Arrangement.spacedBy(spacing.space6)
    ) {
        ProgressHeader(isDemo = state.isDemo)
        when {
            state.isLoading -> {
                Skeleton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SkeletonImpactHeight),
                    shape = SegundoAireTheme.shapes.radiusLg
                )
                Skeleton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SkeletonChartHeight),
                    shape = SegundoAireTheme.shapes.radiusLg
                )
            }
            !state.hasData -> EmptyState(
                title = stringResource(R.string.progress_empty_title),
                message = stringResource(R.string.progress_empty_message),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.space8),
                icon = SegundoAireIcons.Progress
            )
            else -> {
                ImpactSection(minutes = state.recoveredMinutes)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(spacing.space4)
                ) {
                    StatCard(
                        eyebrow = stringResource(R.string.progress_avoided_eyebrow),
                        value = state.avoidedOpenings,
                        label = pluralStringResource(R.plurals.progress_avoided_label, state.avoidedOpenings),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    StatCard(
                        eyebrow = stringResource(R.string.progress_streak_eyebrow),
                        value = state.streakDays,
                        label = pluralStringResource(R.plurals.progress_streak_label, state.streakDays),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
                state.today?.let { today ->
                    WeekCard(week = state.week, today = today, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun ProgressHeader(isDemo: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SegundoAireTheme.spacing.space3)
    ) {
        Text(
            text = stringResource(R.string.progress_title),
            style = SegundoAireTheme.typography.headlineMd,
            color = SegundoAireTheme.colors.onBackground,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )
        if (isDemo) DemoChip()
    }
}

/** Aviso fijo, no interactivo: lo que se ve son datos de ejemplo. */
@Composable
private fun DemoChip(modifier: Modifier = Modifier) {
    val spacing = SegundoAireTheme.spacing
    Box(
        modifier = modifier
            .glass(GlassLevel.Level1, SegundoAireTheme.shapes.radiusSm)
            .padding(horizontal = spacing.space3, vertical = spacing.space2)
    ) {
        Text(
            text = stringResource(R.string.progress_demo_chip),
            style = SegundoAireTheme.typography.labelMd,
            color = SegundoAireTheme.colors.onBackground
        )
    }
}

/** El dato de impacto, sobre `background`: TalkBack lo lee como una sola frase. */
@Composable
private fun ImpactSection(minutes: Int, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(SegundoAireTheme.spacing.space1)
    ) {
        Text(
            text = stringResource(R.string.progress_impact_eyebrow).toUpperCase(Locale.current),
            style = typography.labelSmCaps,
            color = colors.onBackgroundMuted
        )
        AnimatedCounter(
            value = minutes,
            style = typography.displayImpact,
            color = colors.onBackground
        )
        Text(
            text = pluralStringResource(R.plurals.progress_impact_minutes, minutes),
            style = typography.bodyLg,
            color = colors.onBackgroundMuted
        )
    }
}

@Composable
private fun StatCard(
    eyebrow: String,
    value: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    GlassCard(
        modifier = modifier.semantics(mergeDescendants = true) {},
        level = GlassLevel.Level2
    ) {
        OnboardingEyebrow(eyebrow)
        AnimatedCounter(
            value = value,
            style = typography.headlineMd,
            color = colors.onBackground,
            modifier = Modifier.padding(top = SegundoAireTheme.spacing.space2)
        )
        Text(text = label, style = typography.bodyMd, color = colors.onBackground)
    }
}

@Composable
private fun WeekCard(week: List<DayMinutes>, today: LocalDate, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    GlassCard(modifier = modifier, level = GlassLevel.Level2) {
        Text(
            text = stringResource(R.string.progress_week_title),
            style = SegundoAireTheme.typography.titleMd,
            color = colors.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(R.string.progress_week_caption),
            style = SegundoAireTheme.typography.bodyMd,
            color = colors.onBackground
        )
        WeeklyBarChart(
            days = week,
            today = today,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.space4)
        )
    }
}

private val PreviewToday: LocalDate = LocalDate.of(2026, 10, 14)

private val PreviewState = ProgressUiState(
    isLoading = false,
    recoveredMinutes = 295,
    avoidedOpenings = 19,
    streakDays = 7,
    week = listOf(40, 30, 60, 10, 55, 45, 55).mapIndexed { index, minutes ->
        DayMinutes(PreviewToday.minusDays(6L - index), minutes)
    }
)

@Composable
private fun ProgressScreenSample(state: ProgressUiState) {
    ProgressScreen(
        state = state,
        modifier = Modifier.background(SegundoAireTheme.colors.background)
    )
}

@Preview(name = "Progreso · claro", heightDp = 800)
@Composable
private fun ProgressScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) { ProgressScreenSample(PreviewState) }
}

@Preview(name = "Progreso demo · oscuro", heightDp = 800)
@Composable
private fun ProgressScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) { ProgressScreenSample(PreviewState.copy(isDemo = true)) }
}

@Preview(name = "Progreso vacío · claro", heightDp = 800)
@Composable
private fun ProgressScreenEmptyLightPreview() {
    SegundoAireTheme(darkTheme = false) { ProgressScreenSample(ProgressUiState(isLoading = false)) }
}

@Preview(name = "Progreso vacío · oscuro", heightDp = 800)
@Composable
private fun ProgressScreenEmptyDarkPreview() {
    SegundoAireTheme(darkTheme = true) { ProgressScreenSample(ProgressUiState(isLoading = false)) }
}
