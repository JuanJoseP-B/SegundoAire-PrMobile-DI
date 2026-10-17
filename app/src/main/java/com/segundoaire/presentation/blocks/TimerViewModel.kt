package com.segundoaire.presentation.blocks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.model.ActiveBlock
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.repository.BlockRepository
import com.segundoaire.domain.usecase.GetBlockedAppsUseCase
import com.segundoaire.domain.usecase.GetUserProfileUseCase
import com.segundoaire.domain.usecase.ObserveAppUsageTodayUseCase
import com.segundoaire.domain.usecase.ToggleAppBlockStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

/**
 * Bloques y modo enfoque: elige duración y rigor, inicia o termina el bloque activo
 * y vigila las apps. Lo comparten la pestaña Bloques y el modo enfoque del
 * contenedor (el mismo ViewModel de la entrada `home`). El rigor arranca en el
 * sugerido del perfil ("Plan y modo") hasta que el usuario elige otro.
 *
 * El restante nunca se acumula tick a tick: cada emisión es `endsAt - reloj`, así
 * que rotar, salir a otra app o cerrar la pantalla no lo desvía. El tick de un
 * segundo vive dentro de [uiState] y solo corre mientras alguien lo observa.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TimerViewModel @Inject constructor(
    getBlockedApps: GetBlockedAppsUseCase,
    getUserProfile: GetUserProfileUseCase,
    private val toggleAppBlockStatus: ToggleAppBlockStatusUseCase,
    observeAppUsageToday: ObserveAppUsageTodayUseCase,
    private val blockRepository: BlockRepository,
    private val clock: Clock
) : ViewModel() {

    // rigor = null: todavía no eligió, se usa el sugerido del perfil
    private data class Selection(
        val minutes: Int = ActiveBlock.DEFAULT_DURATION_MINUTES,
        val rigor: RigorLevel? = null
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

    private val suggestedRigor = getUserProfile().map { it.suggestedRigor }.distinctUntilChanged()

    // Las apps con sus minutos estimados de hoy
    private val apps = combine(getBlockedApps(), observeAppUsageToday()) { apps, usage ->
        apps.map { WatchedApp(it.packageName, it.appName, it.isBlocked, usage[it.packageName]) }
    }

    val uiState: StateFlow<TimerUiState> = combine(
        selection,
        running,
        apps,
        completedMinutes,
        suggestedRigor
    ) { selection, running, apps, completedMinutes, suggested ->
        // Un bloque en curso manda sobre un completado que quedó sin ver
        val completed = completedMinutes.takeIf { running == null }
        TimerUiState(
            selectedMinutes = running?.block?.durationMinutes ?: selection.minutes,
            rigor = running?.block?.rigor ?: selection.rigor ?: suggested,
            isRunning = running != null,
            remainingMillis = when {
                running != null -> running.remainingMillis
                completed != null -> 0L
                else -> selection.minutes * ActiveBlock.MILLIS_PER_MINUTE
            },
            apps = apps,
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
        val rigor = chosen.rigor ?: uiState.value.rigor
        viewModelScope.launch { blockRepository.start(chosen.minutes, rigor) }
    }

    fun onToggleApp(packageName: String, isBlocked: Boolean) {
        viewModelScope.launch { toggleAppBlockStatus(packageName, isBlocked) }
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
