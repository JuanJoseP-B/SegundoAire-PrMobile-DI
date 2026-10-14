package com.segundoaire.presentation.blocks

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.domain.model.ActiveBlock
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.presentation.designsystem.components.BlockButton
import com.segundoaire.presentation.designsystem.components.HoldToCommitButton
import com.segundoaire.presentation.designsystem.components.PrimaryButton
import com.segundoaire.presentation.designsystem.components.Rigor
import com.segundoaire.presentation.designsystem.components.RigorSelector
import com.segundoaire.presentation.designsystem.components.SegmentedSelector
import com.segundoaire.presentation.designsystem.components.TimerRing
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.toRigor
import java.util.Locale as JavaLocale

/**
 * Bloques: anillo con el restante, duración, rigor y el botón que inicia o
 * termina el bloque. Sin estado propio: [TimerRoute] lo conecta.
 *
 * Con el bloque en curso los selectores se deshabilitan con un fundido. En
 * Strict el botón pasa a ser de mantener presionado: un toque no corta el bloque.
 *
 * Cuando el bloque llega a su fin, los controles dejan paso al estado de completado:
 * el anillo lleno con su halo, los minutos ganados y "Listo" ([onCompletedSeen]).
 */
@Composable
fun TimerScreen(
    state: TimerUiState,
    onDurationSelected: (minutes: Int) -> Unit,
    onRigorSelected: (RigorLevel) -> Unit,
    onStart: () -> Unit,
    onEnd: () -> Unit,
    onCompletedSeen: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val typography = SegundoAireTheme.typography
    val motion = SegundoAireTheme.motion
    val direction = LocalLayoutDirection.current
    val completed = state.completedMinutes != null

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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.space6)
    ) {
        Text(
            text = stringResource(R.string.timer_title),
            style = typography.headlineMd,
            color = colors.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { heading() }
        )

        TimerRing(
            progress = state.progress,
            timeText = formatTime(state.remainingSeconds),
            // space4 + space6 de la columna = el aire de space10 alrededor del anillo
            modifier = Modifier.padding(vertical = spacing.space4),
            label = stringResource(
                when {
                    completed -> R.string.timer_state_completed
                    state.isRunning -> R.string.timer_state_focused
                    else -> R.string.timer_state_ready
                }
            ),
            running = state.isRunning,
            remainingMillis = state.remainingMillis.toInt(),
            completed = completed
        )

        Crossfade(
            targetState = state.completedMinutes,
            animationSpec = motion.calm(),
            label = "timerCompleted"
        ) { completedMinutes ->
            if (completedMinutes != null) {
                BlockCompleted(minutes = completedMinutes, onDone = onCompletedSeen)
            } else {
                BlockControls(state, onDurationSelected, onRigorSelected, onStart, onEnd)
            }
        }
    }
}

@Composable
private fun BlockControls(
    state: TimerUiState,
    onDurationSelected: (minutes: Int) -> Unit,
    onRigorSelected: (RigorLevel) -> Unit,
    onStart: () -> Unit,
    onEnd: () -> Unit
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val typography = SegundoAireTheme.typography
    val motion = SegundoAireTheme.motion

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.space6)
    ) {
        Text(
            text = if (state.watchedAppsCount == 0) {
                stringResource(R.string.timer_no_watched_apps)
            } else {
                pluralStringResource(R.plurals.timer_watched, state.watchedAppsCount, state.watchedAppsCount)
            },
            style = typography.bodyMd,
            color = colors.onBackgroundMuted,
            textAlign = TextAlign.Center
        )

        // Las dos versiones se funden entre sí: los colores deshabilitados no entran de golpe
        Crossfade(
            targetState = !state.isRunning,
            animationSpec = motion.calm(),
            label = "timerSelectors"
        ) { enabled ->
            Column(verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
                SectionLabel(stringResource(R.string.timer_duration_title))
                SegmentedSelector(
                    options = ActiveBlock.DURATION_OPTIONS_MINUTES.map {
                        stringResource(R.string.timer_duration_option, it)
                    },
                    selectedIndex = ActiveBlock.DURATION_OPTIONS_MINUTES.indexOf(state.selectedMinutes),
                    onSelected = { onDurationSelected(ActiveBlock.DURATION_OPTIONS_MINUTES[it]) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled
                )
                SectionLabel(
                    text = stringResource(R.string.timer_rigor_title),
                    modifier = Modifier.padding(top = spacing.space3)
                )
                RigorSelector(
                    selected = state.rigor.toRigor(),
                    onSelected = { onRigorSelected(it.toRigorLevel()) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled
                )
            }
        }

        Crossfade(
            targetState = state.endRequiresHold,
            animationSpec = motion.toggle(),
            label = "timerAction"
        ) { hold ->
            if (hold) {
                HoldToCommitButton(
                    text = stringResource(R.string.timer_hold_to_end),
                    onCommit = onEnd,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Mientras se funde hacia el hold de Strict sigue siendo "Iniciar": nunca termina con un toque
                val canEndWithTap = state.isRunning && !state.endRequiresHold
                BlockButton(
                    active = canEndWithTap,
                    onClick = if (canEndWithTap) onEnd else onStart,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.toUpperCase(Locale.current),
        style = SegundoAireTheme.typography.labelSmCaps,
        color = SegundoAireTheme.colors.onBackgroundMuted,
        modifier = modifier
            .fillMaxWidth()
            .semantics { heading() }
    )
}

/** Éxito sobrio: una sola cifra, los minutos que el bloque le devolvió al usuario. */
@Composable
private fun BlockCompleted(minutes: Int, onDone: () -> Unit) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val typography = SegundoAireTheme.typography

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.space6)
    ) {
        // TalkBack lo lee como una frase: "Bloque completado, 25 min, para ti"
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) {},
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.space1)
        ) {
            Text(
                text = stringResource(R.string.timer_completed_title),
                style = typography.titleLg,
                color = colors.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.timer_duration_option, minutes),
                style = typography.displayImpact,
                color = colors.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.timer_completed_caption),
                style = typography.bodyLg,
                color = colors.onBackgroundMuted,
                textAlign = TextAlign.Center
            )
        }
        PrimaryButton(
            text = stringResource(R.string.timer_completed_done),
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun Rigor.toRigorLevel(): RigorLevel = when (this) {
    Rigor.Mindful -> RigorLevel.MINDFUL
    Rigor.Strict -> RigorLevel.STRICT
}

private fun formatTime(totalSeconds: Int): String =
    String.format(JavaLocale.ROOT, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)

@Composable
private fun TimerScreenSample(state: TimerUiState) {
    TimerScreen(
        state = state,
        onDurationSelected = {},
        onRigorSelected = {},
        onStart = {},
        onEnd = {},
        onCompletedSeen = {},
        modifier = Modifier.background(SegundoAireTheme.colors.background)
    )
}

private val PreviewIdle = TimerUiState(watchedAppsCount = 3)

private val PreviewRunning = TimerUiState(
    selectedMinutes = 30,
    rigor = RigorLevel.STRICT,
    isRunning = true,
    remainingMillis = 18 * ActiveBlock.MILLIS_PER_MINUTE + 42_000,
    watchedAppsCount = 1
)

private val PreviewCompleted = TimerUiState(
    selectedMinutes = 25,
    remainingMillis = 0L,
    watchedAppsCount = 3,
    completedMinutes = 25
)

@Preview(name = "Bloque completado · claro", heightDp = 800)
@Composable
private fun TimerScreenCompletedLightPreview() {
    SegundoAireTheme(darkTheme = false) { TimerScreenSample(PreviewCompleted) }
}

@Preview(name = "Bloque completado · oscuro", heightDp = 800)
@Composable
private fun TimerScreenCompletedDarkPreview() {
    SegundoAireTheme(darkTheme = true) { TimerScreenSample(PreviewCompleted) }
}

@Preview(name = "Bloques listo · claro", heightDp = 800)
@Composable
private fun TimerScreenIdleLightPreview() {
    SegundoAireTheme(darkTheme = false) { TimerScreenSample(PreviewIdle) }
}

@Preview(name = "Bloques listo · oscuro", heightDp = 800)
@Composable
private fun TimerScreenIdleDarkPreview() {
    SegundoAireTheme(darkTheme = true) { TimerScreenSample(PreviewIdle.copy(watchedAppsCount = 0)) }
}

@Preview(name = "Bloque Strict en curso · claro", heightDp = 800)
@Composable
private fun TimerScreenRunningLightPreview() {
    SegundoAireTheme(darkTheme = false) { TimerScreenSample(PreviewRunning) }
}

@Preview(name = "Bloque Mindful en curso · oscuro", heightDp = 800)
@Composable
private fun TimerScreenRunningDarkPreview() {
    SegundoAireTheme(darkTheme = true) { TimerScreenSample(PreviewRunning.copy(rigor = RigorLevel.MINDFUL)) }
}
