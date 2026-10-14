package com.segundoaire.presentation.blocks

import com.segundoaire.domain.model.ActiveBlock
import com.segundoaire.domain.model.BlockedApp
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.usecase.GetBlockedAppsUseCase
import com.segundoaire.testing.FakeBlockRepository
import com.segundoaire.testing.FakeBlockedAppRepository
import com.segundoaire.testing.MutableClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val clock = MutableClock()
    private val blockedApps = FakeBlockedAppRepository(
        listOf(
            BlockedApp("com.example.video", "Videos"),
            BlockedApp("com.example.social", "Social"),
            BlockedApp("com.example.news", "Noticias", isBlocked = false)
        )
    )
    private var blocks = FakeBlockRepository(clock)

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = TimerViewModel(GetBlockedAppsUseCase(blockedApps), blocks, clock)

    // uiState usa WhileSubscribed: el colector hace de pantalla visible
    private fun TestScope.show(viewModel: TimerViewModel): Job =
        backgroundScope.launch(dispatcher) { viewModel.uiState.collect {} }

    private fun TestScope.observed(viewModel: TimerViewModel): TimerViewModel {
        show(viewModel)
        return viewModel
    }

    /** Pasa [millis] reales con la pantalla despierta: avanzan el reloj y los delays. */
    private fun TestScope.elapse(millis: Long) {
        clock.instant += Duration.ofMillis(millis)
        advanceTimeBy(millis)
        runCurrent()
    }

    // --- Selección ---

    @Test
    fun `empieza listo con la duracion por defecto y Mindful`() = runTest(dispatcher) {
        val state = observed(viewModel()).uiState.value

        assertFalse(state.isRunning)
        assertEquals(15, state.selectedMinutes)
        assertEquals(RigorLevel.MINDFUL, state.rigor)
        assertEquals(15 * 60, state.remainingSeconds)
        assertEquals(0f, state.progress)
    }

    @Test
    fun `cuenta solo las apps vigiladas`() = runTest(dispatcher) {
        assertEquals(2, observed(viewModel()).uiState.value.watchedAppsCount)
    }

    @Test
    fun `elegir duracion y rigor actualiza el estado`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())

        viewModel.onDurationSelected(30)
        viewModel.onRigorSelected(RigorLevel.STRICT)

        val state = viewModel.uiState.value
        assertEquals(30, state.selectedMinutes)
        assertEquals(RigorLevel.STRICT, state.rigor)
        assertEquals(30 * 60, state.remainingSeconds)
    }

    // --- Restante desde el reloj ---

    @Test
    fun `iniciar crea el bloque con la seleccion y muestra el total`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())
        viewModel.onDurationSelected(5)
        viewModel.onRigorSelected(RigorLevel.STRICT)

        viewModel.onStart()

        assertEquals(ActiveBlock.starting(clock.millis(), 5, RigorLevel.STRICT), blocks.observeActiveBlock().value)
        val state = viewModel.uiState.value
        assertTrue(state.isRunning)
        assertEquals(5 * 60, state.remainingSeconds)
    }

    @Test
    fun `emite el restante cada segundo`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())
        viewModel.onDurationSelected(5)
        viewModel.onStart()

        elapse(1_000)
        assertEquals(299, viewModel.uiState.value.remainingSeconds)

        elapse(1_000)
        assertEquals(298, viewModel.uiState.value.remainingSeconds)

        elapse(58_000)
        assertEquals(240, viewModel.uiState.value.remainingSeconds)
        assertEquals(0.2f, viewModel.uiState.value.progress, 0.001f)
    }

    @Test
    fun `el restante sale del reloj y no de contar ticks`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())
        viewModel.onDurationSelected(5)
        viewModel.onStart()

        // El proceso estuvo congelado: pasa minuto y medio de reloj pero solo llega un tick
        clock.instant += Duration.ofSeconds(90)
        advanceTimeBy(1_000)
        runCurrent()

        assertEquals(210, viewModel.uiState.value.remainingSeconds)
    }

    @Test
    fun `un tick que llega tarde se vuelve a alinear con el segundo`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())
        viewModel.onDurationSelected(5)
        viewModel.onStart()

        clock.instant += Duration.ofMillis(1_400)
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(299, viewModel.uiState.value.remainingSeconds)

        // Quedan 298.6 s: el siguiente tick llega a los 600 ms, no a los 1000
        elapse(600)
        assertEquals(298, viewModel.uiState.value.remainingSeconds)
    }

    @Test
    fun `sin pantalla visible no emite y al volver retoma desde el reloj`() = runTest(dispatcher) {
        val viewModel = viewModel()
        val screen = show(viewModel)
        viewModel.onDurationSelected(5)
        viewModel.onStart()
        elapse(1_000)
        assertEquals(299, viewModel.uiState.value.remainingSeconds)

        screen.cancel()
        elapse(60_000)
        assertEquals(299, viewModel.uiState.value.remainingSeconds)

        show(viewModel)
        assertEquals(239, viewModel.uiState.value.remainingSeconds)
    }

    @Test
    fun `un ViewModel nuevo retoma el bloque en curso desde el primer estado`() = runTest(dispatcher) {
        blocks = FakeBlockRepository(clock, ActiveBlock.starting(clock.millis(), 10, RigorLevel.STRICT))
        clock.instant += Duration.ofMinutes(4)

        val viewModel = viewModel()

        // Antes de que nadie observe: sin parpadeo en "listo" al rotar
        val initial = viewModel.uiState.value
        assertTrue(initial.isRunning)
        assertEquals(6 * 60, initial.remainingSeconds)
        assertEquals(10, initial.selectedMinutes)
        assertEquals(RigorLevel.STRICT, initial.rigor)

        observed(viewModel)
        elapse(1_000)
        assertEquals(6 * 60 - 1, viewModel.uiState.value.remainingSeconds)
    }

    // --- Completado ---

    @Test
    fun `al pasar endsAt el bloque se marca como completado`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())
        viewModel.onDurationSelected(5)
        viewModel.onStart()

        elapse(299_000)
        assertTrue(viewModel.uiState.value.isRunning)
        assertEquals(1, viewModel.uiState.value.remainingSeconds)
        assertEquals(0, blocks.completeCount)

        elapse(1_000)

        assertEquals(1, blocks.completeCount)
        assertEquals(0, blocks.stopCount)
        assertNull(blocks.observeActiveBlock().value)
        val state = viewModel.uiState.value
        assertFalse(state.isRunning)
        assertEquals(5, state.completedMinutes)
        // El anillo se queda lleno y en cero mientras se muestra el completado
        assertEquals(0, state.remainingSeconds)
        assertEquals(1f, state.progress)
    }

    @Test
    fun `Listo cierra el completado y vuelve a ofrecer la misma seleccion`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())
        viewModel.onDurationSelected(5)
        viewModel.onRigorSelected(RigorLevel.STRICT)
        viewModel.onStart()
        elapse(300_000)

        viewModel.onCompletedSeen()

        val state = viewModel.uiState.value
        assertNull(state.completedMinutes)
        assertNull(blocks.observeCompletedMinutes().value)
        assertFalse(state.isRunning)
        assertEquals(5, state.selectedMinutes)
        assertEquals(RigorLevel.STRICT, state.rigor)
        assertEquals(5 * 60, state.remainingSeconds)
        assertEquals(0f, state.progress)
    }

    @Test
    fun `un bloque completado con la app cerrada aparece desde el primer estado`() = runTest(dispatcher) {
        blocks = FakeBlockRepository(clock, initialCompletedMinutes = 30)

        val viewModel = viewModel()

        val initial = viewModel.uiState.value
        assertEquals(30, initial.completedMinutes)
        assertEquals(1f, initial.progress)
        assertEquals(30, observed(viewModel).uiState.value.completedMinutes)
    }

    @Test
    fun `terminar antes de tiempo no muestra el completado`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())
        viewModel.onStart()
        elapse(30_000)

        viewModel.onEnd()

        assertNull(viewModel.uiState.value.completedMinutes)
    }

    @Test
    fun `un bloque que vencio fuera de la pantalla se completa al volver`() = runTest(dispatcher) {
        val viewModel = viewModel()
        val screen = show(viewModel)
        viewModel.onDurationSelected(5)
        viewModel.onStart()
        screen.cancel()

        elapse(Duration.ofMinutes(6).toMillis())
        assertEquals(0, blocks.completeCount)

        show(viewModel)

        assertEquals(1, blocks.completeCount)
        assertFalse(viewModel.uiState.value.isRunning)
        assertEquals(5, viewModel.uiState.value.completedMinutes)
    }

    @Test
    fun `tras completarse se puede iniciar otro bloque`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())
        viewModel.onDurationSelected(5)
        viewModel.onStart()
        elapse(300_000)

        viewModel.onStart()
        elapse(1_000)

        assertTrue(viewModel.uiState.value.isRunning)
        assertEquals(299, viewModel.uiState.value.remainingSeconds)
    }

    // --- Terminar y rigor ---

    @Test
    fun `en Strict terminar exige mantener presionado`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())
        viewModel.onRigorSelected(RigorLevel.STRICT)
        assertFalse(viewModel.uiState.value.endRequiresHold)

        viewModel.onStart()

        assertTrue(viewModel.uiState.value.endRequiresHold)
    }

    @Test
    fun `en Mindful terminar es un toque`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())
        viewModel.onRigorSelected(RigorLevel.MINDFUL)

        viewModel.onStart()

        assertFalse(viewModel.uiState.value.endRequiresHold)
    }

    @Test
    fun `terminar detiene el bloque sin contarlo como completado`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())
        viewModel.onDurationSelected(10)
        viewModel.onRigorSelected(RigorLevel.STRICT)
        viewModel.onStart()
        elapse(30_000)

        viewModel.onEnd()

        assertEquals(1, blocks.stopCount)
        assertEquals(0, blocks.completeCount)
        val state = viewModel.uiState.value
        assertFalse(state.isRunning)
        assertFalse(state.endRequiresHold)
        // Conserva lo elegido para el siguiente bloque
        assertEquals(10, state.selectedMinutes)
        assertEquals(RigorLevel.STRICT, state.rigor)
        assertEquals(10 * 60, state.remainingSeconds)
    }

    @Test
    fun `con el bloque en curso no se cambia duracion ni rigor ni se reinicia`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())
        viewModel.onDurationSelected(5)
        viewModel.onStart()
        val block = blocks.observeActiveBlock().value
        elapse(10_000)

        viewModel.onDurationSelected(30)
        viewModel.onRigorSelected(RigorLevel.STRICT)
        viewModel.onStart()

        val state = viewModel.uiState.value
        assertEquals(5, state.selectedMinutes)
        assertEquals(RigorLevel.MINDFUL, state.rigor)
        assertEquals(290, state.remainingSeconds)
        assertEquals(block, blocks.observeActiveBlock().value)
    }

    @Test
    fun `terminar sin bloque no hace nada`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())

        viewModel.onEnd()

        assertEquals(0, blocks.stopCount)
    }
}
