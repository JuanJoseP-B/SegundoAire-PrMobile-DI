package com.segundoaire.presentation.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.usecase.GetBlockedAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fase 7: solo UI. La cuenta regresiva simula el progreso del bloque; todavía no
 * se conecta con el motor de intercepción ni persiste la sesión.
 */
@HiltViewModel
class TimerViewModel @Inject constructor(
    getBlockedApps: GetBlockedAppsUseCase
) : ViewModel() {

    private val config = MutableStateFlow(TimerUiState())
    private var tickJob: Job? = null

    val uiState: StateFlow<TimerUiState> = combine(
        config,
        getBlockedApps()
    ) { state, apps ->
        state.copy(watchedAppsCount = apps.count { it.isBlocked })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimerUiState())

    fun onDurationSelected(minutes: Int) {
        config.update {
            if (it.isRunning) it else it.copy(selectedMinutes = minutes, remainingSeconds = minutes * 60)
        }
    }

    fun onRigorSelected(level: RigorLevel) {
        config.update { if (it.isRunning) it else it.copy(rigorLevel = level) }
    }

    fun onToggleRunning() {
        if (config.value.isRunning) stop() else start()
    }

    private fun start() {
        config.update { it.copy(isRunning = true, remainingSeconds = it.totalSeconds) }
        tickJob = viewModelScope.launch {
            while (config.value.remainingSeconds > 0) {
                delay(1_000)
                config.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
            stop()
        }
    }

    private fun stop() {
        tickJob?.cancel()
        tickJob = null
        config.update { it.copy(isRunning = false, remainingSeconds = it.totalSeconds) }
    }
}
