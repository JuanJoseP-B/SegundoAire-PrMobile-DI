package com.segundoaire.presentation.panel

import com.segundoaire.domain.model.BlockSession
import com.segundoaire.domain.model.CoachAction
import com.segundoaire.domain.model.CoachMessage
import com.segundoaire.domain.model.CoachTopic
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.GreetingPeriod
import com.segundoaire.domain.model.InterceptionEvent
import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.TreeState
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.domain.usecase.CheckAccessibilityEnabledUseCase
import com.segundoaire.domain.usecase.CheckOverlayPermissionUseCase
import com.segundoaire.domain.usecase.GetGreetingPeriodUseCase
import com.segundoaire.domain.usecase.GetUserProfileUseCase
import com.segundoaire.domain.usecase.ObserveTodayStatsUseCase
import com.segundoaire.presentation.designsystem.theme.Motion
import com.segundoaire.testing.FakeAccessibilityStatusRepository
import com.segundoaire.testing.FakeBlockRepository
import com.segundoaire.testing.FakeBlockSessionRepository
import com.segundoaire.testing.FakeCoachRepository
import com.segundoaire.testing.FakeInterceptionEventRepository
import com.segundoaire.testing.FakeOverlayPermissionRepository
import com.segundoaire.testing.FakeTreeRepository
import com.segundoaire.testing.FakeUserProfileRepository
import com.segundoaire.testing.MutableClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class PanelViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val clock = MutableClock(Instant.parse("2026-10-13T15:00:00Z"))
    private val profile = FakeUserProfileRepository()
    private val accessibility = FakeAccessibilityStatusRepository(enabled = true)
    private val overlay = FakeOverlayPermissionRepository(granted = true)
    private val sessions = FakeBlockSessionRepository()
    private val interceptions = FakeInterceptionEventRepository()
    private val trees = FakeTreeRepository()
    private val blocks = FakeBlockRepository(clock)
    private val coach = FakeCoachRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = PanelViewModel(
        getUserProfile = GetUserProfileUseCase(profile),
        observeTodayStats = ObserveTodayStatsUseCase(sessions, interceptions, clock),
        trees = trees,
        blocks = blocks,
        coach = coach,
        getGreetingPeriod = GetGreetingPeriodUseCase(clock),
        checkOverlayPermission = CheckOverlayPermissionUseCase(overlay),
        checkAccessibilityEnabled = CheckAccessibilityEnabledUseCase(accessibility),
        clock = clock
    )

    // uiState usa WhileSubscribed: sin un colector no hay estado nuevo
    private fun TestScope.observed(viewModel: PanelViewModel): PanelViewModel {
        backgroundScope.launch(dispatcher) { viewModel.uiState.collect {} }
        return viewModel
    }

    private fun at(time: String) = Instant.parse(time).toEpochMilli()

    // --- Encabezado y cifras ---

    @Test
    fun `muestra el nombre y la hora local`() = runTest(dispatcher) {
        profile.saveProfile(UserProfile(name = "Ana"))
        val viewModel = observed(viewModel())

        assertEquals("Ana", viewModel.uiState.value.name)
        assertEquals(15, viewModel.uiState.value.hour)
    }

    @Test
    fun `saluda segun la hora del reloj`() = runTest(dispatcher) {
        val expected = mapOf(
            8 to GreetingPeriod.MORNING,
            15 to GreetingPeriod.AFTERNOON,
            19 to GreetingPeriod.EVENING,
            3 to GreetingPeriod.EVENING
        )
        expected.forEach { (hour, period) ->
            clock.setHour(hour)
            val viewModel = observed(viewModel())

            assertEquals("a las $hour h", period, viewModel.uiState.value.greeting)
        }
    }

    @Test
    fun `al volver a la app actualiza el saludo y la hora`() = runTest(dispatcher) {
        clock.setHour(9)
        val viewModel = observed(viewModel())
        assertEquals(GreetingPeriod.MORNING, viewModel.uiState.value.greeting)

        clock.setHour(21)
        viewModel.onResume()

        assertEquals(GreetingPeriod.EVENING, viewModel.uiState.value.greeting)
        assertEquals(21, viewModel.uiState.value.hour)
    }

    @Test
    fun `las cifras de hoy salen de los bloques y de las intercepciones de hoy`() = runTest(dispatcher) {
        sessions.sessions.value = listOf(
            BlockSession(at("2026-10-13T09:00:00Z"), at("2026-10-13T09:15:00Z"), 15, RigorLevel.MINDFUL, completed = true),
            BlockSession(at("2026-10-12T09:00:00Z"), at("2026-10-12T09:30:00Z"), 30, RigorLevel.MINDFUL, completed = true)
        )
        interceptions.events.value = listOf(
            InterceptionEvent(1, "a", at("2026-10-13T10:00:00Z"), InterceptionResult.VOLVIO),
            InterceptionEvent(2, "a", at("2026-10-13T11:00:00Z"), InterceptionResult.CONTINUO),
            InterceptionEvent(3, "a", at("2026-10-13T12:00:00Z"), InterceptionResult.VOLVIO),
            InterceptionEvent(4, "a", at("2026-10-12T12:00:00Z"), InterceptionResult.VOLVIO)
        )
        val stats = observed(viewModel()).uiState.value.stats

        assertEquals(15, stats.recoveredMinutes)
        assertEquals(2, stats.pausesRespected)
        assertEquals(3, stats.openingsAvoided)
    }

    // --- Árbol ---

    @Test
    fun `el arbol viene del repositorio para el dia de hoy`() = runTest(dispatcher) {
        trees.tree.value = TreeState(goals = listOf(Goal.SLEEP), weekLeaves = 3, seenLeaves = 1)
        val state = observed(viewModel()).uiState.value

        assertEquals(2, state.tree.newLeaves)
        assertEquals(LocalDate.of(2026, 10, 13), trees.requestedDays.last())
    }

    @Test
    fun `al brotar las hojas quedan marcadas como vistas`() = runTest(dispatcher) {
        trees.tree.value = TreeState(weekLeaves = 3, seenLeaves = 1)
        val viewModel = observed(viewModel())

        viewModel.onLeavesShown(3)

        assertEquals(0, viewModel.uiState.value.tree.newLeaves)
    }

    // --- Bloque en curso ---

    @Test
    fun `muestra el bloque en curso y no uno vencido`() = runTest(dispatcher) {
        blocks.start(15, RigorLevel.MINDFUL)
        val viewModel = observed(viewModel())
        assertEquals(15, viewModel.uiState.value.activeBlock?.durationMinutes)

        // Nadie retiró el bloque vencido: al volver a la app ya no se muestra
        clock.instant = clock.instant.plusSeconds(16 * 60)
        viewModel.onResume()

        assertNull(viewModel.uiState.value.activeBlock)
    }

    // --- Coach ---

    @Test
    fun `el coach piensa 1,4 s antes de mostrar el mensaje`() = runTest(dispatcher) {
        coach.reply = { _, _ -> CoachMessage.GoalIdea(Goal.SLEEP) }
        val viewModel = observed(viewModel())
        assertTrue(viewModel.uiState.value.coach.isThinking)

        advanceTimeBy(Motion.CoachThinkingMillis - 1)
        assertTrue(viewModel.uiState.value.coach.isThinking)

        advanceTimeBy(2)
        val coachState = viewModel.uiState.value.coach
        assertFalse(coachState.isThinking)
        assertEquals(CoachMessage.GoalIdea(Goal.SLEEP), coachState.message)
        assertEquals(1, coachState.revision)
    }

    @Test
    fun `otra sugerencia pide la siguiente con el mensaje anterior`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())
        advanceUntilIdle()

        viewModel.onCoachNext()
        advanceUntilIdle()

        assertEquals(null to CoachMessage.FirstSteps, coach.requests.last())
        assertEquals(2, viewModel.uiState.value.coach.revision)
    }

    @Test
    fun `mientras piensa no se pide otra sugerencia`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())

        viewModel.onCoachNext()
        advanceUntilIdle()

        assertEquals(1, coach.requests.size)
    }

    @Test
    fun `un chip de preguntale pide su tema aunque este pensando`() = runTest(dispatcher) {
        coach.reply = { topic, _ -> if (topic == CoachTopic.HARDEST_TIME) CoachMessage.HardestHour(21, 4) else CoachMessage.FirstSteps }
        val viewModel = observed(viewModel())

        viewModel.onCoachAsk(CoachTopic.HARDEST_TIME)
        advanceUntilIdle()

        assertEquals(CoachTopic.HARDEST_TIME, coach.requests.last().first)
        assertEquals(CoachMessage.HardestHour(21, 4), viewModel.uiState.value.coach.message)
    }

    @Test
    fun `si el coach falla invita a empezar en vez de quedar en blanco`() = runTest(dispatcher) {
        coach.failure = IllegalStateException("sin datos")
        val viewModel = observed(viewModel())
        advanceUntilIdle()

        assertEquals(CoachMessage.FirstSteps, viewModel.uiState.value.coach.message)
        assertFalse(viewModel.uiState.value.coach.isThinking)
    }

    @Test
    fun `las acciones del coach llevan a su destino`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())

        viewModel.onCoachAction(CoachAction.CREATE_BLOCK)
        assertEquals(PanelEffect.OpenBlocks, viewModel.effect.first())
        viewModel.onCoachAction(CoachAction.VIEW_PROGRESS)
        assertEquals(PanelEffect.OpenProgress, viewModel.effect.first())
        viewModel.onCoachAction(CoachAction.REMIND)
        assertEquals(PanelEffect.RemindToast, viewModel.effect.first())
    }

    @Test
    fun `iniciar 15 min arranca un bloque con el rigor sugerido y lo abre`() = runTest(dispatcher) {
        profile.saveProfile(UserProfile(name = "Ana", suggestedRigor = RigorLevel.STRICT))
        val viewModel = observed(viewModel())

        viewModel.onCoachAction(CoachAction.START_15)

        assertEquals(PanelEffect.OpenActiveBlock, viewModel.effect.first())
        val block = blocks.observeActiveBlock().value
        assertEquals(15, block?.durationMinutes)
        assertEquals(RigorLevel.STRICT, block?.rigor)
    }

    @Test
    fun `iniciar 15 min con un bloque en curso no lo reemplaza`() = runTest(dispatcher) {
        blocks.start(30, RigorLevel.MINDFUL)
        val viewModel = observed(viewModel())

        viewModel.onCoachAction(CoachAction.START_15)

        assertEquals(PanelEffect.OpenActiveBlock, viewModel.effect.first())
        assertEquals(30, blocks.observeActiveBlock().value?.durationMinutes)
    }

    // --- Permisos ---

    @Test
    fun `con ambos permisos no falta ninguno`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())

        assertNull(viewModel.uiState.value.missingPermission)
    }

    @Test
    fun `sin accesibilidad pide primero accesibilidad`() = runTest(dispatcher) {
        accessibility.enabled = false
        overlay.granted = false
        val viewModel = observed(viewModel())

        assertEquals(MissingPermission.ACCESSIBILITY, viewModel.uiState.value.missingPermission)
    }

    @Test
    fun `con accesibilidad pero sin superposicion pide superposicion`() = runTest(dispatcher) {
        overlay.granted = false
        val viewModel = observed(viewModel())

        assertEquals(MissingPermission.OVERLAY, viewModel.uiState.value.missingPermission)
    }

    @Test
    fun `al volver de Ajustes el permiso concedido deja de faltar`() = runTest(dispatcher) {
        accessibility.enabled = false
        val viewModel = observed(viewModel())
        assertEquals(MissingPermission.ACCESSIBILITY, viewModel.uiState.value.missingPermission)

        accessibility.enabled = true
        viewModel.onResume()

        assertNull(viewModel.uiState.value.missingPermission)
    }

    @Test
    fun `refleja un permiso revocado al volver`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())

        overlay.granted = false
        viewModel.onResume()

        assertEquals(MissingPermission.OVERLAY, viewModel.uiState.value.missingPermission)
    }
}
