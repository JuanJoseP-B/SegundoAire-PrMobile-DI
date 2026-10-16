package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.GreetingPeriod
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class GetGreetingPeriodUseCaseTest {

    private fun periodAt(hour: Int, minute: Int = 0): GreetingPeriod {
        val instant = Instant.parse("2026-10-13T%02d:%02d:00Z".format(hour, minute))
        return GetGreetingPeriodUseCase(Clock.fixed(instant, ZoneOffset.UTC))()
    }

    @Test
    fun `de 5 a 11 saluda por la manana`() {
        assertEquals(GreetingPeriod.MORNING, periodAt(5))
        assertEquals(GreetingPeriod.MORNING, periodAt(11, 59))
    }

    @Test
    fun `de 12 a 18 saluda por la tarde`() {
        assertEquals(GreetingPeriod.AFTERNOON, periodAt(12))
        assertEquals(GreetingPeriod.AFTERNOON, periodAt(18, 59))
    }

    @Test
    fun `de 19 a 4 saluda por la noche`() {
        assertEquals(GreetingPeriod.EVENING, periodAt(19))
        assertEquals(GreetingPeriod.EVENING, periodAt(20))
        assertEquals(GreetingPeriod.EVENING, periodAt(0))
        assertEquals(GreetingPeriod.EVENING, periodAt(4, 59))
    }

    @Test
    fun `usa la hora local de la zona del reloj`() {
        val instant = Instant.parse("2026-10-13T02:00:00Z")
        // 02:00 UTC son las 20:00 del día anterior en Ciudad de México
        val clock = Clock.fixed(instant, ZoneId.of("America/Mexico_City"))

        assertEquals(GreetingPeriod.EVENING, GetGreetingPeriodUseCase(clock)())
    }
}
