package com.segundoaire.presentation.progress

import com.segundoaire.domain.model.BlockSession
import com.segundoaire.domain.model.InterceptionEvent
import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.TreeState
import com.segundoaire.domain.usecase.AperturasEvitadasUseCase
import com.segundoaire.domain.usecase.HoraMasDificilUseCase
import com.segundoaire.domain.usecase.MejorRachaUseCase
import com.segundoaire.domain.usecase.MinutosPorDiaUseCase
import com.segundoaire.domain.usecase.ObserveDemoModeUseCase
import com.segundoaire.testing.FakeBlockSessionRepository
import com.segundoaire.testing.FakeInterceptionEventRepository
import com.segundoaire.testing.FakeTreeRepository
import com.segundoaire.testing.FakeUserPreferencesRepository
import com.segundoaire.testing.MutableClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class ProgressViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    // Martes 2026-10-13 23:00 UTC: la semana va del lunes 12 al domingo 18
    private val clock = MutableClock(Instant.parse("2026-10-13T23:00:00Z"))
    private val sessions = FakeBlockSessionRepository()
    private val events = FakeInterceptionEventRepository()
    private val preferences = FakeUserPreferencesRepository()
    private val trees = FakeTreeRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = ProgressViewModel(
        MinutosPorDiaUseCase(sessions, clock),
        AperturasEvitadasUseCase(events, clock),
        MejorRachaUseCase(sessions, clock),
        HoraMasDificilUseCase(events, clock),
        ObserveDemoModeUseCase(preferences),
        trees,
        clock
    )

    // uiState usa WhileSubscribed: sin un colector no hay estado nuevo
    private fun TestScope.observed(viewModel: ProgressViewModel): ProgressViewModel {
        backgroundScope.launch(dispatcher) { viewModel.uiState.collect {} }
        return viewModel
    }

    private fun session(endedAt: String, minutes: Int) = Instant.parse(endedAt).toEpochMilli().let { end ->
        BlockSession(end - minutes * 60_000L, end, minutes, RigorLevel.MINDFUL, completed = true)
    }

    private fun event(id: Long, at: String, result: InterceptionResult) =
        InterceptionEvent(id, "com.instagram.android", Instant.parse(at).toEpochMilli(), result)

    @Test
    fun `empieza cargando`() {
        assertTrue(viewModel().uiState.value.isLoading)
    }

    @Test
    fun `la semana va de lunes a domingo y los dias que no llegan no tienen datos`() = runTest(dispatcher) {
        sessions.record(session("2026-10-12T20:00:00Z", 30))
        sessions.record(session("2026-10-13T08:00:00Z", 15))

        val state = observed(viewModel()).uiState.value

        assertFalse(state.isLoading)
        assertEquals(LocalDate.of(2026, 10, 12), state.week.first().date)
        assertEquals(LocalDate.of(2026, 10, 18), state.week.last().date)
        assertEquals(listOf(30, 15, null, null, null, null, null), state.week.map { it.minutes })
        assertEquals(45, state.weekMinutes)
    }

    @Test
    fun `de entrada se elige hoy`() = runTest(dispatcher) {
        val state = observed(viewModel()).uiState.value

        assertEquals(LocalDate.of(2026, 10, 13), state.selectedDay)
    }

    @Test
    fun `tocar un dia pasado lo elige y uno futuro no`() = runTest(dispatcher) {
        sessions.record(session("2026-10-12T20:00:00Z", 30))
        val viewModel = observed(viewModel())

        viewModel.onDaySelected(LocalDate.of(2026, 10, 12))
        assertEquals(LocalDate.of(2026, 10, 12), viewModel.uiState.value.selectedDay)
        assertEquals(30, viewModel.uiState.value.selectedMinutes)

        viewModel.onDaySelected(LocalDate.of(2026, 10, 16))
        assertEquals(LocalDate.of(2026, 10, 12), viewModel.uiState.value.selectedDay)
    }

    @Test
    fun `un anillo por dia desde que se planto, al menos la semana y hasta doce`() = runTest(dispatcher) {
        trees.tree.value = TreeState(plantedAt = LocalDate.of(2026, 10, 13))
        val viewModel = observed(viewModel())
        assertEquals(2, viewModel.uiState.value.rings.size)

        trees.tree.value = TreeState(plantedAt = LocalDate.of(2026, 10, 8))
        assertEquals(6, viewModel.uiState.value.rings.size)

        trees.tree.value = TreeState(plantedAt = LocalDate.of(2026, 8, 1))
        assertEquals(12, viewModel.uiState.value.rings.size)
        assertEquals(LocalDate.of(2026, 10, 13), viewModel.uiState.value.rings.last().date)
    }

    @Test
    fun `reune aperturas de la semana, mejor racha y hora mas dificil`() = runTest(dispatcher) {
        sessions.record(session("2026-10-01T20:00:00Z", 15))
        sessions.record(session("2026-10-02T20:00:00Z", 15))
        sessions.record(session("2026-10-03T20:00:00Z", 15))
        sessions.record(session("2026-10-13T08:00:00Z", 15))
        events.events.value = listOf(
            event(1, "2026-10-13T21:00:00Z", InterceptionResult.VOLVIO),
            event(2, "2026-10-12T21:30:00Z", InterceptionResult.CONTINUO),
            event(3, "2026-10-09T08:00:00Z", InterceptionResult.VOLVIO)
        )

        val state = observed(viewModel()).uiState.value

        assertEquals(2, state.weekOpenings)
        assertEquals(3, state.bestStreak)
        assertEquals(21, state.hardestHour)
    }

    @Test
    fun `sin intercepciones no hay hora mas dificil`() = runTest(dispatcher) {
        assertNull(observed(viewModel()).uiState.value.hardestHour)
    }

    @Test
    fun `indica cuando se muestran datos de ejemplo`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())

        preferences.setDemoMode(true)
        assertTrue(viewModel.uiState.value.isDemo)

        preferences.setDemoMode(false)
        assertFalse(viewModel.uiState.value.isDemo)
    }

    @Test
    fun `al volver despues de medianoche la semana avanza`() = runTest(dispatcher) {
        sessions.record(session("2026-10-13T08:00:00Z", 15))
        val viewModel = observed(viewModel())

        clock.instant = Instant.parse("2026-10-14T07:00:00Z")
        viewModel.onResume()

        val state = viewModel.uiState.value
        assertEquals(LocalDate.of(2026, 10, 14), state.today)
        assertEquals(listOf(0, 15, 0, null, null, null, null), state.week.map { it.minutes })
    }
}
