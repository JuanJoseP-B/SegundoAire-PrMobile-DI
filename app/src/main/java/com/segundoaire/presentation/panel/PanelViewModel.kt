package com.segundoaire.presentation.panel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.model.CoachAction
import com.segundoaire.domain.model.CoachMessage
import com.segundoaire.domain.model.CoachTopic
import com.segundoaire.domain.model.GreetingPeriod
import com.segundoaire.domain.repository.BlockRepository
import com.segundoaire.domain.repository.CoachRepository
import com.segundoaire.domain.repository.TreeRepository
import com.segundoaire.domain.usecase.CheckAccessibilityEnabledUseCase
import com.segundoaire.domain.usecase.CheckOverlayPermissionUseCase
import com.segundoaire.domain.usecase.GetGreetingPeriodUseCase
import com.segundoaire.domain.usecase.GetUserProfileUseCase
import com.segundoaire.domain.usecase.ObserveTodayStatsUseCase
import com.segundoaire.presentation.designsystem.theme.Motion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * Panel: saludo, árbol, cifras de hoy, bloque en curso y coach, en un único
 * [PanelUiState]. Las acciones del coach que salen del Panel llegan como [PanelEffect].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PanelViewModel @Inject constructor(
    getUserProfile: GetUserProfileUseCase,
    observeTodayStats: ObserveTodayStatsUseCase,
    private val trees: TreeRepository,
    private val blocks: BlockRepository,
    private val coach: CoachRepository,
    private val getGreetingPeriod: GetGreetingPeriodUseCase,
    private val checkOverlayPermission: CheckOverlayPermissionUseCase,
    private val checkAccessibilityEnabled: CheckAccessibilityEnabledUseCase,
    private val clock: Clock
) : ViewModel() {

    // Lo que cambia al volver a la app: permisos concedidos en Ajustes, la hora y el día
    private data class Environment(
        val hasOverlayPermission: Boolean,
        val isAccessibilityEnabled: Boolean,
        val greeting: GreetingPeriod,
        val hour: Int,
        val today: LocalDate
    )

    private val profile = getUserProfile()
    private val environment = MutableStateFlow(readEnvironment())
    private val coachState = MutableStateFlow(CoachUiState())
    private var coachJob: Job? = null
    private val effects = Channel<PanelEffect>(Channel.BUFFERED)

    // Las cifras y el árbol se fijan al día: solo se vuelven a pedir si cambió
    private val today: Flow<LocalDate> = environment.map { it.today }.distinctUntilChanged()
    private val stats = today.flatMapLatest { observeTodayStats() }
    private val tree = today.flatMapLatest { trees.observeTree(it) }

    private val content = combine(profile, environment, stats, tree, blocks.observeActiveBlock()) { profile, env, stats, tree, block ->
        PanelUiState(
            greeting = env.greeting,
            hour = env.hour,
            name = profile.name,
            stats = stats,
            tree = tree,
            activeBlock = block?.takeIf { it.isActiveAt(clock.millis()) },
            hasOverlayPermission = env.hasOverlayPermission,
            isAccessibilityEnabled = env.isAccessibilityEnabled
        )
    }

    val uiState: StateFlow<PanelUiState> = combine(content, coachState) { state, coach -> state.copy(coach = coach) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), PanelUiState())

    /** Efectos de una sola vez: los consume [PanelRoute]. */
    val effect: Flow<PanelEffect> = effects.receiveAsFlow()

    init {
        requestCoach(topic = null)
    }

    /** Al volver de Ajustes o de otra app (ON_RESUME): relee permisos, hora y día. */
    fun onResume() {
        environment.value = readEnvironment()
    }

    /** Las hojas nuevas ya brotaron en pantalla: la próxima vez se dibujan sin animar. */
    fun onLeavesShown(count: Int) {
        viewModelScope.launch { trees.markLeavesSeen(environment.value.today, count) }
    }

    /** "Otra sugerencia". Se ignora mientras ya piensa. */
    fun onCoachNext() {
        if (!coachState.value.isThinking) requestCoach(topic = null)
    }

    /** Un chip de "Pregúntale": cambia el tema aunque esté pensando otro. */
    fun onCoachAsk(topic: CoachTopic) {
        requestCoach(topic)
    }

    fun onCoachAction(action: CoachAction) {
        when (action) {
            CoachAction.NEXT -> onCoachNext()
            CoachAction.CREATE_BLOCK -> effects.trySend(PanelEffect.OpenBlocks)
            CoachAction.VIEW_PROGRESS -> effects.trySend(PanelEffect.OpenProgress)
            CoachAction.REMIND -> effects.trySend(PanelEffect.RemindToast)
            CoachAction.START_15 -> startQuickBlock()
        }
    }

    fun onActiveBlockClick() {
        effects.trySend(PanelEffect.OpenActiveBlock)
    }

    // Con un bloque en curso no se pisa: se abre el que ya corre
    private fun startQuickBlock() {
        viewModelScope.launch {
            val running = blocks.observeActiveBlock().value?.takeIf { it.isActiveAt(clock.millis()) }
            if (running == null) {
                blocks.start(QUICK_BLOCK_MINUTES, profile.first().suggestedRigor)
            }
            effects.send(PanelEffect.OpenActiveBlock)
        }
    }

    private fun requestCoach(topic: CoachTopic?) {
        val previous = coachState.value.message
        coachJob?.cancel()
        coachState.update { it.copy(isThinking = true) }
        coachJob = viewModelScope.launch {
            val next = async {
                try {
                    coach.message(topic, previous)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Sin datos legibles el coach no se queda en blanco: invita a empezar
                    previous ?: CoachMessage.FirstSteps
                }
            }
            // El coach "piensa" siempre lo mismo, aunque los datos lleguen antes
            delay(Motion.CoachThinkingMillis)
            val message = next.await()
            coachState.update { CoachUiState(message = message, isThinking = false, revision = it.revision + 1) }
        }
    }

    private fun readEnvironment() = Environment(
        hasOverlayPermission = checkOverlayPermission(),
        isAccessibilityEnabled = checkAccessibilityEnabled(),
        greeting = getGreetingPeriod(),
        hour = LocalTime.now(clock).hour,
        today = LocalDate.now(clock)
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val QUICK_BLOCK_MINUTES = 15
    }
}
