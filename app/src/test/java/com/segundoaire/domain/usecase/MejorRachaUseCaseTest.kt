package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.InterceptionEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class MejorRachaUseCaseTest {

    private val monday = LocalDate.of(2026, 10, 12)

    @Test
    fun `sin dias no hay racha`() {
        assertEquals(0, MejorRachaUseCase.longestStreak(emptySet()))
    }

    @Test
    fun `gana el tramo mas largo aunque no sea el ultimo`() {
        val days = setOf(
            monday.minusDays(10), monday.minusDays(9), monday.minusDays(8), monday.minusDays(7),
            monday, monday.plusDays(1)
        )

        assertEquals(4, MejorRachaUseCase.longestStreak(days))
    }

    @Test
    fun `un dia suelto es una racha de uno`() {
        assertEquals(1, MejorRachaUseCase.longestStreak(setOf(monday)))
    }

    @Test
    fun `la hora mas dificil es la de mas intercepciones y en empate la mas tardia`() {
        fun at(time: String) = InterceptionEvent(0, "a", Instant.parse(time).toEpochMilli(), null)
        val events = listOf(at("2026-10-12T08:00:00Z"), at("2026-10-13T21:10:00Z"), at("2026-10-12T08:40:00Z"), at("2026-10-14T21:59:00Z"))

        assertEquals(21 to 2, HoraMasDificilUseCase.hardestHour(events, ZoneOffset.UTC))
        assertNull(HoraMasDificilUseCase.hardestHour(emptyList(), ZoneOffset.UTC))
    }
}
