package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.InterceptionEvent
import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.testing.FakeInterceptionEventRepository
import com.segundoaire.testing.MutableClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class AperturasEvitadasUseCaseTest {

    // Martes 2026-10-13 09:00 UTC: la semana empieza el miércoles 7 a las 00:00
    private val clock = MutableClock()

    private fun event(id: Long, at: String, result: InterceptionResult?) =
        InterceptionEvent(id, "com.instagram.android", Instant.parse(at).toEpochMilli(), result)

    private fun useCase(vararg events: InterceptionEvent) =
        AperturasEvitadasUseCase(FakeInterceptionEventRepository(events.toList()), clock)

    @Test
    fun `sin intercepciones no hay aperturas evitadas`() = runTest {
        assertEquals(0, useCase()().first())
    }

    @Test
    fun `cuenta solo las veces que el usuario volvio`() = runTest {
        val useCase = useCase(
            event(1, "2026-10-13T08:00:00Z", InterceptionResult.VOLVIO),
            event(2, "2026-10-12T08:00:00Z", InterceptionResult.CONTINUO),
            event(3, "2026-10-11T08:00:00Z", InterceptionResult.VOLVIO),
            event(4, "2026-10-13T08:59:00Z", result = null)
        )

        assertEquals(2, useCase().first())
    }

    @Test
    fun `lo anterior a la semana no cuenta`() = runTest {
        val useCase = useCase(
            event(1, "2026-10-06T23:59:00Z", InterceptionResult.VOLVIO),
            event(2, "2026-10-07T00:00:00Z", InterceptionResult.VOLVIO)
        )

        assertEquals(1, useCase().first())
    }

    @Test
    fun `con un dia cuenta solo hoy`() = runTest {
        val useCase = useCase(
            event(1, "2026-10-13T00:01:00Z", InterceptionResult.VOLVIO),
            event(2, "2026-10-12T23:59:00Z", InterceptionResult.VOLVIO)
        )

        assertEquals(1, useCase(days = 1).first())
    }

    @Test
    fun `una intercepcion que se resuelve como volvio pasa a contar`() = runTest {
        val repository = FakeInterceptionEventRepository()
        val avoided = AperturasEvitadasUseCase(repository, clock)()
        val id = repository.record("com.instagram.android", clock.millis())
        assertEquals(0, avoided.first())

        repository.setResult(id, InterceptionResult.VOLVIO)

        assertEquals(1, avoided.first())
    }
}
