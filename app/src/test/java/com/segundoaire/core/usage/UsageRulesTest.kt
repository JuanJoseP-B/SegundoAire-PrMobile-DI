package com.segundoaire.core.usage

import com.segundoaire.domain.model.AppUsageSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class UsageRulesTest {

    private val zone = ZoneOffset.UTC
    private fun at(time: String) = Instant.parse(time).toEpochMilli()

    @Test
    fun `un rato normal queda tal cual`() {
        val sessions = UsageRules.close("a", at("2026-10-16T10:00:00Z"), at("2026-10-16T10:12:00Z"), zone)

        assertEquals(listOf(AppUsageSession("a", at("2026-10-16T10:00:00Z"), at("2026-10-16T10:12:00Z"))), sessions)
    }

    @Test
    fun `un rato que cruza la medianoche se parte en dos dias`() {
        val sessions = UsageRules.close("a", at("2026-10-16T23:50:00Z"), at("2026-10-17T00:20:00Z"), zone)

        assertEquals(2, sessions.size)
        assertEquals(10 * 60_000L, sessions[0].durationMillis)
        assertEquals(20 * 60_000L, sessions[1].durationMillis)
        assertEquals(at("2026-10-17T00:00:00Z"), sessions[1].startedAt)
    }

    @Test
    fun `los huecos anormales se descartan`() {
        // Más de tres horas seguidas: un apagado de pantalla que no llegó
        assertTrue(UsageRules.close("a", at("2026-10-16T08:00:00Z"), at("2026-10-16T11:30:00Z"), zone).isEmpty())
        // El reloj volvió atrás
        assertTrue(UsageRules.close("a", at("2026-10-16T10:00:00Z"), at("2026-10-16T09:00:00Z"), zone).isEmpty())
        // Menos de un segundo es ruido
        assertTrue(UsageRules.close("a", at("2026-10-16T10:00:00Z"), at("2026-10-16T10:00:00Z") + 400, zone).isEmpty())
    }
}
