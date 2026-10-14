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
