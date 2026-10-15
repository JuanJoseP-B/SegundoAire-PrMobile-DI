package com.segundoaire.presentation.progress

import com.segundoaire.domain.model.BlockSession
import com.segundoaire.domain.model.InterceptionEvent
import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.usecase.AperturasEvitadasUseCase
import com.segundoaire.domain.usecase.MinutosPorDiaUseCase
import com.segundoaire.domain.usecase.MinutosRecuperadosUseCase
import com.segundoaire.domain.usecase.ObserveDemoModeUseCase
import com.segundoaire.domain.usecase.RachaUseCase
import com.segundoaire.testing.FakeBlockSessionRepository
import com.segundoaire.testing.FakeInterceptionEventRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    // Martes 2026-10-13 23:00 UTC: la semana va del miércoles 7 al martes 13
    private val clock = MutableClock(Instant.parse("2026-10-13T23:00:00Z"))
    private val sessions = FakeBlockSessionRepository()
    private val events = FakeInterceptionEventRepository()
    private val preferences = FakeUserPreferencesRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = ProgressViewModel(
        MinutosRecuperadosUseCase(sessions, clock),
        AperturasEvitadasUseCase(events, clock),
        RachaUseCase(sessions, clock),
        MinutosPorDiaUseCase(sessions, clock),
        ObserveDemoModeUseCase(preferences),
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
    fun `sin historial no hay datos que mostrar`() = runTest(dispatcher) {
        val state = observed(viewModel()).uiState.value

        assertFalse(state.isLoading)
        assertFalse(state.hasData)
        assertEquals(7, state.week.size)
        assertEquals(LocalDate.of(2026, 10, 13), state.today)
    }

    @Test
    fun `reune minutos, aperturas, racha y la semana`() = runTest(dispatcher) {
        sessions.record(session("2026-10-12T20:00:00Z", 30))
        sessions.record(session("2026-10-13T08:00:00Z", 15))
        events.events.value = listOf(
            event(1, "2026-10-13T10:00:00Z", InterceptionResult.VOLVIO),
            event(2, "2026-10-13T11:00:00Z", InterceptionResult.CONTINUO),
            event(3, "2026-10-09T11:00:00Z", InterceptionResult.VOLVIO)
        )

        val state = observed(viewModel()).uiState.value

        assertEquals(45, state.recoveredMinutes)
        assertEquals(2, state.avoidedOpenings)
        assertEquals(2, state.streakDays)
        assertEquals(listOf(0, 0, 0, 0, 0, 30, 15), state.week.map { it.minutes })
        assertTrue(state.hasData)
        assertFalse(state.isDemo)
    }

    @Test
    fun `se actualiza al completar un bloque`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())

        sessions.record(session("2026-10-13T08:00:00Z", 25))

        assertEquals(25, viewModel.uiState.value.recoveredMinutes)
        assertEquals(1, viewModel.uiState.value.streakDays)
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
        sessions.record(session("2026-10-07T10:00:00Z", 30))
        sessions.record(session("2026-10-13T08:00:00Z", 15))
        val viewModel = observed(viewModel())
        assertEquals(45, viewModel.uiState.value.recoveredMinutes)

        clock.instant = Instant.parse("2026-10-14T07:00:00Z")
        viewModel.onResume()

        val state = viewModel.uiState.value
        assertEquals(15, state.recoveredMinutes)
        assertEquals(LocalDate.of(2026, 10, 14), state.today)
        assertEquals(listOf(0, 0, 0, 0, 0, 15, 0), state.week.map { it.minutes })
    }
}
