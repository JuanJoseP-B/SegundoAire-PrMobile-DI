package com.segundoaire.core.service

import com.segundoaire.domain.model.InterceptionEvent
import com.segundoaire.domain.model.InterceptionIntent
import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.testing.FakeInterceptionEventRepository
import com.segundoaire.testing.MutableClock
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration

class InterceptionRecorderTest {

    private val scope = TestScope(UnconfinedTestDispatcher())
    private val clock = MutableClock()
    private val repository = FakeInterceptionEventRepository()
    private val recorder = InterceptionRecorder(repository, clock, scope.backgroundScope)

    private val events: List<InterceptionEvent> get() = repository.events.value

    @Test
    fun `al mostrarse guarda la intercepcion sin resultado`() = scope.runTest {
        recorder.onShown("com.instagram.android")

        assertEquals(
            listOf(InterceptionEvent(1, "com.instagram.android", clock.millis(), result = null)),
            events
        )
    }

    @Test
    fun `al cerrarse guarda el resultado`() = scope.runTest {
        recorder.onShown("com.instagram.android")

        recorder.onClosed(InterceptionResult.CONTINUO)

        assertEquals(InterceptionResult.CONTINUO, events.single().result)
    }

    @Test
    fun `cerrar sin intercepcion abierta no hace nada`() = scope.runTest {
        recorder.onClosed(InterceptionResult.VOLVIO)

        assertEquals(emptyList<InterceptionEvent>(), events)
    }

    @Test
    fun `solo el primer cierre cuenta`() = scope.runTest {
        recorder.onShown("com.instagram.android")

        recorder.onClosed(InterceptionResult.CONTINUO)
        recorder.onClosed(InterceptionResult.VOLVIO)

        assertEquals(InterceptionResult.CONTINUO, events.single().result)
    }

    @Test
    fun `otra intercepcion encima cierra la anterior como que volvio`() = scope.runTest {
        recorder.onShown("com.instagram.android")
        val first = clock.millis()
        clock.instant += Duration.ofSeconds(3)

        recorder.onShown("com.zhiliaoapp.musically")

        assertEquals(
            listOf(
                InterceptionEvent(1, "com.instagram.android", first, InterceptionResult.VOLVIO),
                InterceptionEvent(2, "com.zhiliaoapp.musically", clock.millis(), result = null)
            ),
            events
        )
    }

    @Test
    fun `la intencion elegida queda en la intercepcion abierta`() = scope.runTest {
        recorder.onShown("com.instagram.android")

        recorder.onIntent(InterceptionIntent.BORED)

        assertEquals(InterceptionIntent.BORED, events.single().intent)
    }

    @Test
    fun `sin intercepcion abierta la intencion no se guarda`() = scope.runTest {
        recorder.onIntent(InterceptionIntent.SEARCH)

        assertEquals(emptyList<InterceptionEvent>(), events)
    }
}
