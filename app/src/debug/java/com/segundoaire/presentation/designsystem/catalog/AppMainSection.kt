package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.domain.model.CoachMessage
import com.segundoaire.domain.model.DayMinutes
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.GreetingPeriod
import com.segundoaire.domain.model.TodayStats
import com.segundoaire.domain.model.TreeState
import com.segundoaire.presentation.blocks.TimerScreen
import com.segundoaire.presentation.blocks.TimerUiState
import com.segundoaire.presentation.blocks.WatchedApp
import com.segundoaire.presentation.designsystem.components.SecondaryButton
import com.segundoaire.presentation.designsystem.theme.Motion
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.main.MainShellContent
import com.segundoaire.presentation.main.MainTab
import com.segundoaire.presentation.panel.CoachUiState
import com.segundoaire.presentation.panel.PanelScreen
import com.segundoaire.presentation.panel.PanelUiState
import com.segundoaire.presentation.profile.ProfileScreen
import com.segundoaire.presentation.profile.ProfileUiState
import com.segundoaire.presentation.progress.ProgressScreen
import com.segundoaire.presentation.progress.ProgressUiState
import com.segundoaire.presentation.progress.WeekDay
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime

private const val SampleWeekLeaves = 3

/**
 * La app principal con datos de ejemplo y sin ViewModels: la barra inferior cambia de
 * pestaña de verdad y "Repetir" vuelve a mostrar la entrada del Panel (el coach piensa
 * 1,4 s y revela su mensaje, y brota una hoja nueva).
 */
@Composable
internal fun AppMainSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_app_main)) {
        var replays by rememberSaveable { mutableIntStateOf(0) }
        var tab by rememberSaveable { mutableStateOf(MainTab.PANEL) }
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp

        Box(
            Modifier
                .fillMaxWidth()
                .height(screenHeight)
                .clip(SegundoAireTheme.shapes.radiusLg)
        ) {
            MainShellContent(selectedTab = tab, onTabSelected = { tab = it }) { current, padding ->
                when (current) {
                    MainTab.PANEL -> SamplePanel(replays = replays, padding = padding)
                    MainTab.BLOCKS -> TimerScreen(
                        state = SampleBlocks,
                        onDurationSelected = {},
                        onRigorSelected = {},
                        onStart = {},
                        onOpenFocus = {},
                        onToggleApp = { _, _ -> },
                        onEditApps = {},
                        contentPadding = padding
                    )
                    MainTab.PROGRESS -> ProgressScreen(state = sampleProgress(), onDaySelected = {}, contentPadding = padding)
                    MainTab.PROFILE -> ProfileScreen(
                        state = SampleProfile,
                        onEditGoals = {},
                        onOpenPlan = {},
                        onSoundChange = {},
                        onOpenPermissions = {},
                        onOpenAccount = {},
                        onShare = {},
                        contentPadding = padding
                    )
                }
            }
        }
        SecondaryButton(
            text = stringResource(R.string.catalog_reveal_replay),
            onClick = {
                tab = MainTab.PANEL
                replays++
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// El Panel de ejemplo: cada repetición deja una hoja sin ver y el coach vuelve a pensar
@Composable
private fun SamplePanel(replays: Int, padding: PaddingValues) {
    var seen by remember(replays) { mutableIntStateOf(SampleWeekLeaves - 1) }
    var coach by remember(replays) { mutableStateOf(CoachUiState()) }
    LaunchedEffect(replays) {
        delay(Motion.CoachThinkingMillis)
        coach = CoachUiState(message = CoachMessage.PausesToday(today = 3, yesterday = 1), isThinking = false, revision = replays + 1)
    }
    PanelScreen(
        state = PanelUiState(
            greeting = GreetingPeriod.at(LocalTime.now().hour),
            hour = LocalTime.now().hour,
            name = stringResource(R.string.catalog_sample_name),
            stats = TodayStats(recoveredMinutes = 47, pausesRespected = 3, openingsAvoided = 12),
            tree = TreeState(goals = listOf(Goal.FREE_TIME, Goal.SLEEP), weekLeaves = SampleWeekLeaves, seenLeaves = seen),
            coach = coach
        ),
        onFixPermission = {},
        onLeavesShown = { seen = it },
        onActiveBlockClick = {},
        onCoachNext = {},
        onCoachAsk = {},
        onCoachAction = {},
        contentPadding = padding
    )
}

private val SampleBlocks = TimerUiState(
    apps = listOf(
        WatchedApp("a", "Videos", isBlocked = true, minutesToday = 48),
        WatchedApp("b", "Social", isBlocked = true, minutesToday = 31),
        WatchedApp("c", "Chat", isBlocked = true, minutesToday = 22),
        WatchedApp("d", "Juegos", isBlocked = false, minutesToday = 9)
    )
)

private val SampleProfile = ProfileUiState(
    isLoading = false,
    savedName = "Ana",
    tree = TreeState(goals = listOf(Goal.FREE_TIME, Goal.SLEEP), plantedAt = LocalDate.now().minusDays(12))
)

private val SampleMinutes = listOf(22, 35, 12, 40, 28, 15, 38, 32, 18, 45, 52, 47)

private fun sampleProgress(): ProgressUiState {
    val today = LocalDate.now()
    val daysThisWeek = today.dayOfWeek.value
    val rings = SampleMinutes.mapIndexed { i, m -> DayMinutes(today.minusDays((SampleMinutes.size - 1 - i).toLong()), m) }
    val week = (0 until 7).map { i ->
        val date = today.minusDays((daysThisWeek - 1).toLong()).plusDays(i.toLong())
        WeekDay(date, if (date.isAfter(today)) null else rings.firstOrNull { it.date == date }?.minutes ?: 0)
    }
    return ProgressUiState(
        isLoading = false,
        today = today,
        rings = rings,
        week = week,
        selectedDay = today,
        weekMinutes = week.sumOf { it.minutes ?: 0 },
        weekOpenings = 41,
        bestStreak = 5,
        hardestHour = 21
    )
}
