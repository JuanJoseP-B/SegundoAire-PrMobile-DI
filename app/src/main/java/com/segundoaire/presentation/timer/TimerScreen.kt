package com.segundoaire.presentation.timer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segundoaire.R
import com.segundoaire.presentation.components.TimerRing
import com.segundoaire.presentation.theme.SegundoAireTheme
import java.util.Locale

@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TimerContent(
        state = state,
        onDurationSelected = viewModel::onDurationSelected,
        onRigorSelected = viewModel::onRigorSelected,
        onToggleRunning = viewModel::onToggleRunning,
        modifier = modifier
    )
}

@Composable
private fun TimerContent(
    state: TimerUiState,
    onDurationSelected: (Int) -> Unit,
    onRigorSelected: (RigorLevel) -> Unit,
    onToggleRunning: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(R.string.timer_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )

            TimerRing(
                progress = state.progress,
                timeText = formatTime(state.remainingSeconds),
                stateLabel = stringResource(
                    if (state.isRunning) R.string.timer_state_focused else R.string.timer_state_ready
                ),
                isActive = state.isRunning
            )

            Text(
                text = stringResource(R.string.timer_watched_apps, state.watchedAppsCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            SectionTitle(stringResource(R.string.timer_duration_title))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimerUiState.DURATION_OPTIONS.forEach { minutes ->
                    OptionCard(
                        selected = state.selectedMinutes == minutes,
                        enabled = !state.isRunning,
                        onClick = { onDurationSelected(minutes) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.timer_duration_option, minutes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            SectionTitle(stringResource(R.string.timer_rigor_title))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RigorCard(
                    title = stringResource(R.string.timer_rigor_mindful),
                    description = stringResource(R.string.timer_rigor_mindful_desc),
                    selected = state.rigorLevel == RigorLevel.MINDFUL,
                    enabled = !state.isRunning,
                    onClick = { onRigorSelected(RigorLevel.MINDFUL) },
                    modifier = Modifier.weight(1f)
                )
                RigorCard(
                    title = stringResource(R.string.timer_rigor_strict),
                    description = stringResource(R.string.timer_rigor_strict_desc),
                    selected = state.rigorLevel == RigorLevel.STRICT,
                    enabled = !state.isRunning,
                    onClick = { onRigorSelected(RigorLevel.STRICT) },
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = onToggleRunning,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(if (state.isRunning) R.string.timer_stop else R.string.timer_start),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun OptionCard(
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) accent.copy(alpha = 0.18f) else SegundoAireTheme.glass.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) accent else SegundoAireTheme.glass.border
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) { content() }
    }
}

@Composable
private fun RigorCard(
    title: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OptionCard(selected = selected, enabled = enabled, onClick = onClick, modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatTime(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = totalSeconds % 3600 / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
    }
}
