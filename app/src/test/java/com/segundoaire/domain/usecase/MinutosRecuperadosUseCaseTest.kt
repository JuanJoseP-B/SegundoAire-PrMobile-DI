package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.BlockSession
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.testing.FakeBlockSessionRepository
import com.segundoaire.testing.MutableClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class MinutosRecuperadosUseCaseTest {

    // Martes 2026-10-13 09:00 UTC: la semana empieza el miércoles 7 a las 00:00
    private val clock = MutableClock()

    private fun session(endedAt: String, minutes: Int, completed: Boolean = true): BlockSession {
        val end = Instant.parse(endedAt).toEpochMilli()
        return BlockSession(end - minutes * 60_000L, end, minutes, RigorLevel.MINDFUL, completed)
    }

    private fun useCase(vararg sessions: BlockSession) =
        MinutosRecuperadosUseCase(FakeBlockSessionRepository(sessions.toList()), clock)

    @Test
    fun `sin bloques no hay minutos`() = runTest {
        assertEquals(0, useCase()().first())
    }

    @Test
    fun `suma los bloques completados de la semana`() = runTest {
        val useCase = useCase(
            session("2026-10-13T08:30:00Z", 15),
            session("2026-10-10T20:00:00Z", 30),
            session("2026-10-07T00:00:00Z", 5)
        )

        assertEquals(50, useCase().first())
    }

    @Test
    fun `lo que termino antes de la semana no cuenta`() = runTest {
        val useCase = useCase(
            session("2026-10-06T23:59:00Z", 30),
            session("2026-10-12T10:00:00Z", 10)
        )

        assertEquals(10, useCase().first())
    }

    @Test
    fun `los bloques detenidos antes de tiempo no suman`() = runTest {
        val useCase = useCase(
            session("2026-10-13T08:00:00Z", 30, completed = false),
            session("2026-10-13T07:00:00Z", 15)
        )

        assertEquals(15, useCase().first())
    }

    @Test
    fun `con un dia cuenta solo hoy`() = runTest {
        val useCase = useCase(
            session("2026-10-13T00:10:00Z", 10),
            session("2026-10-12T23:50:00Z", 30)
        )

        assertEquals(10, useCase(days = 1).first())
    }

    @Test
    fun `se actualiza al completar otro bloque`() = runTest {
        val repository = FakeBlockSessionRepository(listOf(session("2026-10-12T10:00:00Z", 10)))
        val minutes = MinutosRecuperadosUseCase(repository, clock)()

        repository.record(session("2026-10-13T08:00:00Z", 15))

        assertEquals(25, minutes.first())
    }
}
