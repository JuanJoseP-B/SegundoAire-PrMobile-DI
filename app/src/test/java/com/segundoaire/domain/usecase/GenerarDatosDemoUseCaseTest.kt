package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.DemoData
import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.testing.FakeBlockSessionRepository
import com.segundoaire.testing.FakeInterceptionEventRepository
import com.segundoaire.testing.MutableClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class GenerarDatosDemoUseCaseTest {

    private val clock = MutableClock(Instant.parse("2026-10-13T23:59:00Z"))

    private fun generate(): DemoData = GenerarDatosDemoUseCase(clock)()

    private fun dateOf(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

    @Test
    fun `cubre siete dias que terminan hoy`() {
        val data = generate()

        val days = data.sessions.filter { it.completed }.map { dateOf(it.endedAt) }.toSortedSet()
        assertEquals((7..13).map { LocalDate.of(2026, 10, it) }, days.toList())
        assertTrue(data.events.all { dateOf(it.timestampMillis) in days })
    }

    @Test
    fun `todo cae dentro de la ventana de progreso`() {
        val start = progressWindowStart(clock, PROGRESS_WINDOW_DAYS)
        val data = generate()

        assertTrue(data.sessions.all { it.endedAt >= start && it.startedAt < it.endedAt })
        assertTrue(data.events.all { it.timestampMillis >= start })
    }

    @Test
    fun `de hoy solo aparece lo que ya paso`() {
        clock.instant = Instant.parse("2026-10-13T09:00:00Z")
        val data = generate()

        assertTrue(data.sessions.all { it.endedAt <= clock.millis() })
        assertTrue(data.events.all { it.timestampMillis <= clock.millis() })
        assertEquals(1, data.sessions.count { dateOf(it.endedAt) == LocalDate.of(2026, 10, 13) })
    }

    @Test
    fun `recien empezado el dia no hay nada de hoy`() {
        clock.instant = Instant.parse("2026-10-13T00:05:00Z")
        val data = generate()

        assertTrue(data.sessions.none { dateOf(it.endedAt) == LocalDate.of(2026, 10, 13) })
        assertTrue(data.events.none { dateOf(it.timestampMillis) == LocalDate.of(2026, 10, 13) })
    }

    @Test
    fun `es determinista`() {
        assertEquals(generate(), generate())
    }

    @Test
    fun `mezcla resultados y bloques detenidos como en un uso real`() {
        val data = generate()

        assertTrue(data.sessions.any { !it.completed })
        assertTrue(data.events.any { it.result == InterceptionResult.VOLVIO })
        assertTrue(data.events.any { it.result == InterceptionResult.CONTINUO })
        assertEquals(data.events.size, data.events.map { it.id }.toSet().size)
        assertEquals(data.sessions.size, data.sessions.map { it.startedAt }.toSet().size)
    }

    @Test
    fun `los calculos de progreso dan cifras de una semana completa`() = runTest {
        val data = generate()
        val sessions = FakeBlockSessionRepository(data.sessions)
        val events = FakeInterceptionEventRepository(data.events)

        assertEquals(295, MinutosRecuperadosUseCase(sessions, clock)().first())
        assertEquals(19, AperturasEvitadasUseCase(events, clock)().first())
        assertEquals(7, RachaUseCase(sessions, clock)().first())
        assertTrue(MinutosPorDiaUseCase(sessions, clock)().first().all { it.minutes > 0 })
    }
}
