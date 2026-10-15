package com.segundoaire.presentation.blocks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.model.ActiveBlock
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.repository.BlockRepository
import com.segundoaire.domain.usecase.GetBlockedAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

/**
 * Bloques: elige duración y rigor, e inicia o termina el bloque activo.
 *
 * El restante nunca se acumula tick a tick: cada emisión es `endsAt - reloj`, así
 * que rotar, salir a otra app o cerrar la pantalla no lo desvía. El tick de un
 * segundo vive dentro de [uiState] y solo corre mientras alguien lo observa.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TimerViewModel @Inject constructor(
    getBlockedApps: GetBlockedAppsUseCase,
    private val blockRepository: BlockRepository,
    private val clock: Clock
) : ViewModel() {

    private data class Selection(
        val minutes: Int = ActiveBlock.DEFAULT_DURATION_MINUTES,
        val rigor: RigorLevel = RigorLevel.MINDFUL
    )

    private data class Running(val block: ActiveBlock, val remainingMillis: Long)

    private val activeBlock = blockRepository.observeActiveBlock()
    private val completedMinutes = blockRepository.observeCompletedMinutes()
    private val selection = MutableStateFlow(Selection())

    private val running: Flow<Running?> = activeBlock.flatMapLatest { block ->
        if (block == null) {
            flowOf(null)
        } else {
            flow {
                // Un bloque restaurado tras cerrar la app trae su propia duración y rigor
                selection.value = Selection(block.durationMinutes, block.rigor)
                while (true) {
                    val remaining = block.remainingMillis(clock.millis())
                    if (remaining <= 0L) break
                    emit(Running(block, remaining))
                    // Despierta justo cuando cambia el segundo que se muestra
                    val untilNextSecond = remaining % TICK_MILLIS
                    delay(if (untilNextSecond == 0L) TICK_MILLIS else untilNextSecond)
                }
                blockRepository.complete()
            }
        }
    }

    val uiState: StateFlow<TimerUiState> = combine(
        selection,
        running,
        getBlockedApps(),
        completedMinutes
    ) { selection, running, apps, completedMinutes ->
        // Un bloque en curso manda sobre un completado que quedó sin ver
        val completed = completedMinutes.takeIf { running == null }
        TimerUiState(
            selectedMinutes = running?.block?.durationMinutes ?: selection.minutes,
            rigor = running?.block?.rigor ?: selection.rigor,
            isRunning = running != null,
            remainingMillis = when {
                running != null -> running.remainingMillis
                completed != null -> 0L
                else -> selection.minutes * ActiveBlock.MILLIS_PER_MINUTE
            },
            watchedAppsCount = apps.count { it.isBlocked },
            completedMinutes = completed
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialState())

    fun onDurationSelected(minutes: Int) {
        if (activeBlock.value != null) return
        selection.update { it.copy(minutes = minutes) }
    }

    fun onRigorSelected(rigor: RigorLevel) {
        if (activeBlock.value != null) return
        selection.update { it.copy(rigor = rigor) }
    }

    fun onStart() {
        if (activeBlock.value != null) return
        val chosen = selection.value
        viewModelScope.launch { blockRepository.start(chosen.minutes, chosen.rigor) }
    }

    /** Termina el bloque antes de tiempo. En Strict la pantalla solo lo llama tras el hold. */
    fun onEnd() {
        if (activeBlock.value == null) return
        viewModelScope.launch { blockRepository.stop() }
    }

    /** "Listo" en el estado de completado: la pantalla vuelve a ofrecer un bloque nuevo. */
    fun onCompletedSeen() {
        if (completedMinutes.value == null) return
        viewModelScope.launch { blockRepository.acknowledgeCompleted() }
    }

    // Evita un primer cuadro en "listo" cuando ya hay un bloque en curso (rotación, volver al tab)
    private fun initialState(): TimerUiState {
        val block = activeBlock.value ?: return idleState()
        val remaining = block.remainingMillis(clock.millis())
        if (remaining <= 0L) return idleState()
        return TimerUiState(
            selectedMinutes = block.durationMinutes,
            rigor = block.rigor,
            isRunning = true,
            remainingMillis = remaining
        )
    }

    private fun idleState(): TimerUiState {
        val completed = completedMinutes.value ?: return TimerUiState()
        return TimerUiState(remainingMillis = 0L, completedMinutes = completed)
    }

    private companion object {
        const val TICK_MILLIS = 1_000L
    }
}
