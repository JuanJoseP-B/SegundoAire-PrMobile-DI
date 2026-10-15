package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.BlockSession
import com.segundoaire.domain.model.DayMinutes
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.testing.FakeBlockSessionRepository
import com.segundoaire.testing.MutableClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MinutosPorDiaUseCaseTest {

    // Martes 2026-10-13 09:00 UTC: la semana va del miércoles 7 al martes 13
    private val clock = MutableClock()

    private fun session(endedAt: String, minutes: Int, completed: Boolean = true): BlockSession {
        val end = Instant.parse(endedAt).toEpochMilli()
        return BlockSession(end - minutes * 60_000L, end, minutes, RigorLevel.MINDFUL, completed)
    }

    private suspend fun week(vararg sessions: BlockSession, days: Int = 7, clock: Clock = this.clock) =
        MinutosPorDiaUseCase(FakeBlockSessionRepository(sessions.toList()), clock)(days).first()

    private fun day(date: String, minutes: Int) = DayMinutes(LocalDate.parse(date), minutes)

    @Test
    fun `sin bloques devuelve siete dias en cero hasta hoy`() = runTest {
        assertEquals(
            listOf(
                day("2026-10-07", 0),
                day("2026-10-08", 0),
                day("2026-10-09", 0),
                day("2026-10-10", 0),
                day("2026-10-11", 0),
                day("2026-10-12", 0),
                day("2026-10-13", 0)
            ),
            week()
        )
    }

    @Test
    fun `suma los bloques completados de cada dia`() = runTest {
        val result = week(
            session("2026-10-13T08:00:00Z", 15),
            session("2026-10-13T06:00:00Z", 25),
            session("2026-10-09T20:00:00Z", 30),
            session("2026-10-09T21:00:00Z", 10, completed = false),
            session("2026-10-06T22:00:00Z", 45)
        )

        assertEquals(day("2026-10-09", 30), result[2])
        assertEquals(day("2026-10-13", 40), result.last())
        assertEquals(70, result.sumOf { it.minutes })
    }

    @Test
    fun `con menos dias la ventana es mas corta`() = runTest {
        assertEquals(
            listOf(day("2026-10-12", 0), day("2026-10-13", 15)),
            week(session("2026-10-13T08:00:00Z", 15), days = 2)
        )
    }

    @Test
    fun `el dia de cada bloque se toma en la zona del reloj`() = runTest {
        // 03:00 UTC del 13 son las 21:00 del 12 en Ciudad de México
        val mexico = Clock.fixed(Instant.parse("2026-10-13T18:00:00Z"), ZoneId.of("America/Mexico_City"))

        val result = week(session("2026-10-13T03:00:00Z", 20), clock = mexico)

        assertEquals(day("2026-10-12", 20), result[5])
        assertEquals(day("2026-10-13", 0), result[6])
    }
}
