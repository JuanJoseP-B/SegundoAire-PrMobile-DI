package com.segundoaire.core.engine

import com.segundoaire.domain.model.BlockedApp
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.usecase.GetBlockedAppsUseCase
import com.segundoaire.testing.FakeBlockRepository
import com.segundoaire.testing.FakeBlockedAppRepository
import com.segundoaire.testing.MutableClock
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class InterceptorEngineTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val clock = MutableClock()
    private val apps = FakeBlockedAppRepository(
        listOf(
            BlockedApp(VIDEO, "Videos", cooldownMinutes = 5),
            BlockedApp(SOCIAL, "Social", cooldownMinutes = 5),
            BlockedApp(NEWS, "Noticias", isBlocked = false)
        )
    )
    private val blocks = FakeBlockRepository(clock)
    private var uptime = 10_000L

    private fun TestScope.engine() = InterceptorEngine(
        GetBlockedAppsUseCase(apps),
        blocks,
        clock,
        ElapsedRealtime { uptime },
        backgroundScope
    ).also { it.start() }

    // --- Sin bloque: el comportamiento de siempre ---

    @Test
    fun `sin bloque intercepta las apps vigiladas`() = runTest(dispatcher) {
        val engine = engine()

        assertTrue(engine.shouldIntercept(VIDEO))
        assertTrue(engine.shouldIntercept(SOCIAL))
    }

    @Test
    fun `nunca intercepta apps apagadas o desconocidas`() = runTest(dispatcher) {
        val engine = engine()
        assertFalse(engine.shouldIntercept(NEWS))
        assertFalse(engine.shouldIntercept("com.example.maps"))

        blocks.start(durationMinutes = 15, rigor = RigorLevel.STRICT)

        assertFalse(engine.shouldIntercept(NEWS))
        assertFalse(engine.shouldIntercept("com.example.maps"))
    }

    @Test
    fun `sin bloque Continuar pausa solo esa app durante su cooldown`() = runTest(dispatcher) {
        val engine = engine()

        engine.pause(VIDEO)

        assertFalse(engine.shouldIntercept(VIDEO))
        assertTrue(engine.shouldIntercept(SOCIAL))

        uptime += Duration.ofMinutes(5).toMillis()

        assertTrue(engine.shouldIntercept(VIDEO))
    }

    // --- Con bloque activo ---

    @Test
    fun `con bloque activo intercepta tambien las apps en pausa`() = runTest(dispatcher) {
        val engine = engine()
        engine.pause(VIDEO)

        blocks.start(durationMinutes = 15, rigor = RigorLevel.MINDFUL)

        assertTrue(engine.shouldIntercept(VIDEO))
        assertTrue(engine.shouldIntercept(SOCIAL))
    }

    @Test
    fun `durante el bloque Continuar no abre una pausa`() = runTest(dispatcher) {
        val engine = engine()
        blocks.start(durationMinutes = 15, rigor = RigorLevel.STRICT)

        engine.pause(VIDEO)

        assertTrue(engine.shouldIntercept(VIDEO))
    }

    @Test
    fun `al terminar el bloque vuelve a respetar la pausa`() = runTest(dispatcher) {
        val engine = engine()
        engine.pause(VIDEO)
        blocks.start(durationMinutes = 15, rigor = RigorLevel.MINDFUL)
        assertTrue(engine.shouldIntercept(VIDEO))

        blocks.stop()

        assertFalse(engine.shouldIntercept(VIDEO))
    }

    @Test
    fun `un bloque vencido que nadie retiro cuenta como sin bloque`() = runTest(dispatcher) {
        val engine = engine()
        blocks.start(durationMinutes = 5, rigor = RigorLevel.STRICT)
        uptime += Duration.ofMinutes(4).toMillis()
        engine.pause(VIDEO)
        clock.instant += Duration.ofMinutes(4)
        assertTrue(engine.shouldIntercept(VIDEO))

        clock.instant += Duration.ofMinutes(1)

        assertFalse(engine.shouldIntercept(VIDEO))
    }

    @Test
    fun `una app que se empieza a vigilar durante el bloque tambien se intercepta`() = runTest(dispatcher) {
        val engine = engine()
        blocks.start(durationMinutes = 15, rigor = RigorLevel.MINDFUL)

        apps.setBlocked(NEWS, isBlocked = true)

        assertTrue(engine.shouldIntercept(NEWS))
    }

    private companion object {
        const val VIDEO = "com.example.video"
        const val SOCIAL = "com.example.social"
        const val NEWS = "com.example.news"
    }
}
