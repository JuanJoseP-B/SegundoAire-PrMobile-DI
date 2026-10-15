package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.BlockSession
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.testing.FakeBlockSessionRepository
import com.segundoaire.testing.MutableClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class RachaUseCaseTest {

    // Martes 2026-10-13 09:00 UTC
    private val clock = MutableClock()

    private fun session(endedAt: String, completed: Boolean = true): BlockSession {
        val end = Instant.parse(endedAt).toEpochMilli()
        return BlockSession(end - 15 * 60_000L, end, 15, RigorLevel.STRICT, completed)
    }

    private suspend fun streak(vararg sessions: BlockSession, clock: Clock = this.clock): Int =
        RachaUseCase(FakeBlockSessionRepository(sessions.toList()), clock)().first()

    @Test
    fun `sin bloques no hay racha`() = runTest {
        assertEquals(0, streak())
    }

    @Test
    fun `cuenta los dias seguidos hasta hoy`() = runTest {
        assertEquals(
            3,
            streak(
                session("2026-10-13T08:00:00Z"),
                session("2026-10-12T21:00:00Z"),
                session("2026-10-11T07:00:00Z")
            )
        )
    }

    @Test
    fun `varios bloques el mismo dia cuentan como uno`() = runTest {
        assertEquals(
            2,
            streak(
                session("2026-10-13T08:00:00Z"),
                session("2026-10-13T06:00:00Z"),
                session("2026-10-12T21:00:00Z"),
                session("2026-10-12T09:00:00Z")
            )
        )
    }

    @Test
    fun `si hoy aun no hay bloque la racha sigue desde ayer`() = runTest {
        assertEquals(
            2,
            streak(
                session("2026-10-12T21:00:00Z"),
                session("2026-10-11T07:00:00Z")
            )
        )
    }

    @Test
    fun `un dia entero sin bloques rompe la racha`() = runTest {
        assertEquals(0, streak(session("2026-10-11T07:00:00Z")))
    }

    @Test
    fun `un hueco deja solo los dias posteriores`() = runTest {
        assertEquals(
            1,
            streak(
                session("2026-10-13T08:00:00Z"),
                session("2026-10-11T07:00:00Z"),
                session("2026-10-10T07:00:00Z")
            )
        )
    }

    @Test
    fun `los bloques detenidos no sostienen la racha`() = runTest {
        assertEquals(
            1,
            streak(
                session("2026-10-13T08:00:00Z"),
                session("2026-10-12T21:00:00Z", completed = false)
            )
        )
    }

    @Test
    fun `la racha sigue al cruzar de una semana a otra`() = runTest {
        // Del sábado 3 al martes 13: once días seguidos, pasando por dos lunes
        val days = (3..13).map { session("2026-10-%02dT08:00:00Z".format(it)) }

        assertEquals(11, streak(*days.toTypedArray()))
    }

    @Test
    fun `el lunes sin bloque todavia mantiene la racha del domingo`() = runTest {
        val repository = FakeBlockSessionRepository(
            listOf(
                session("2026-10-16T20:00:00Z"),
                session("2026-10-17T20:00:00Z"),
                session("2026-10-18T20:00:00Z")
            )
        )
        val streak = RachaUseCase(repository, clock)()
        clock.instant = Instant.parse("2026-10-18T23:00:00Z")
        assertEquals(3, streak.first())

        // Pasa la medianoche del domingo al lunes y aún no hay bloque
        clock.instant = Instant.parse("2026-10-19T08:00:00Z")
        assertEquals(3, streak.first())

        repository.record(session("2026-10-19T09:00:00Z"))
        clock.instant = Instant.parse("2026-10-19T10:00:00Z")
        assertEquals(4, streak.first())

        // Un lunes entero sin bloque la habría roto
        clock.instant = Instant.parse("2026-10-21T10:00:00Z")
        assertEquals(0, streak.first())
    }

    @Test
    fun `el dia de cada bloque se toma en la zona del reloj`() = runTest {
        // 18:00 UTC del 13 son las 12:00 del 13 en Ciudad de México; 03:00 UTC del 13, las 21:00 del 12
        val mexico = Clock.fixed(Instant.parse("2026-10-13T18:00:00Z"), ZoneId.of("America/Mexico_City"))

        assertEquals(
            2,
            streak(
                session("2026-10-13T03:00:00Z"),
                session("2026-10-12T03:00:00Z"),
                clock = mexico
            )
        )
    }
}
