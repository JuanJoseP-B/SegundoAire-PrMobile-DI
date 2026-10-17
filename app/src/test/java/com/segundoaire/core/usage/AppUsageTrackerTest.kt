package com.segundoaire.core.usage

import com.segundoaire.testing.FakeAppUsageRepository
import com.segundoaire.testing.MutableClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class AppUsageTrackerTest {

    private val scope = TestScope(UnconfinedTestDispatcher())
    private val clock = MutableClock(Instant.parse("2026-10-16T10:00:00Z"))
    private val repository = FakeAppUsageRepository()

    private fun tracker() = AppUsageTracker(repository, clock, scope.backgroundScope)

    private fun elapse(minutes: Long) {
        clock.instant += Duration.ofMinutes(minutes)
    }

    @Test
    fun `cambiar de app cierra el rato de la vigilada y lo guarda`() = scope.runTest {
        val tracker = tracker()
        tracker.onForeground("video", watched = true, intercepted = false)
        elapse(12)

        tracker.onForeground("launcher", watched = false, intercepted = false)

        assertEquals(1, repository.recorded.size)
        assertEquals("video", repository.recorded.single().packageName)
        assertEquals(12 * 60_000L, repository.recorded.single().durationMillis)
    }

    @Test
    fun `una app que no se vigila no se mide`() = scope.runTest {
        val tracker = tracker()
        tracker.onForeground("maps", watched = false, intercepted = false)
        elapse(30)
        tracker.onForeground("launcher", watched = false, intercepted = false)

        assertTrue(repository.recorded.isEmpty())
    }

    @Test
    fun `detras del GlassWall no se cuenta hasta continuar`() = scope.runTest {
        val tracker = tracker()
        tracker.onForeground("video", watched = true, intercepted = true)
        elapse(1)
        tracker.onContinue("video")
        elapse(5)
        tracker.onForeground("launcher", watched = false, intercepted = false)

        assertEquals(5 * 60_000L, repository.recorded.single().durationMillis)
    }

    @Test
    fun `apagar la pantalla cierra el rato`() = scope.runTest {
        val tracker = tracker()
        tracker.onForeground("video", watched = true, intercepted = false)
        elapse(3)

        tracker.onScreenOff()
        elapse(60)

        assertEquals(3 * 60_000L, repository.recorded.single().durationMillis)
    }

    @Test
    fun `el corte periodico guarda y sigue midiendo`() = scope.runTest {
        val tracker = tracker()
        tracker.onForeground("video", watched = true, intercepted = false)
        elapse(5)

        tracker.onPeriodicFlush()
        elapse(2)
        tracker.stop()

        assertEquals(listOf(5 * 60_000L, 2 * 60_000L), repository.recorded.map { it.durationMillis })
    }

    @Test
    fun `los minutos de hoy suman lo guardado y el rato abierto`() = scope.runTest {
        repository.usage.value = mapOf("video" to 20 * 60_000L)
        val tracker = tracker()
        tracker.onForeground("video", watched = true, intercepted = false)
        elapse(3)

        assertEquals(23, tracker.minutesToday("video"))
        assertEquals(0, tracker.minutesToday("social"))
    }
}
