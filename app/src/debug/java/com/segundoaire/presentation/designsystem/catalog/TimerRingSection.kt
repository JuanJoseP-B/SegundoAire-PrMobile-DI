package com.segundoaire.presentation.designsystem.catalog

import android.os.SystemClock
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.BlockButton
import com.segundoaire.presentation.designsystem.components.TimerRing
import kotlinx.coroutines.delay

private const val DemoBlockMillis = 60_000

@Composable
internal fun TimerRingSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_timer_ring)) {
        // Bloque de un minuto medido con el reloj, como lo hará la Fase 14
        var running by remember { mutableStateOf(false) }
        var remainingMillis by remember { mutableIntStateOf(DemoBlockMillis) }

        LaunchedEffect(running) {
            if (!running) {
                remainingMillis = DemoBlockMillis
                return@LaunchedEffect
            }
            val end = SystemClock.elapsedRealtime() + DemoBlockMillis
            while (remainingMillis > 0) {
                remainingMillis = (end - SystemClock.elapsedRealtime()).toInt().coerceAtLeast(0)
                delay(1_000)
            }
            running = false
        }

        val seconds = (remainingMillis + 999) / 1_000
        TimerRing(
            progress = 1f - remainingMillis.toFloat() / DemoBlockMillis,
            timeText = stringResource(R.string.catalog_timer_format, seconds / 60, seconds % 60),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            label = stringResource(if (running) R.string.timer_state_focused else R.string.timer_state_ready),
            running = running,
            remainingMillis = remainingMillis
        )
        BlockButton(
            active = running,
            onClick = { running = !running },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
