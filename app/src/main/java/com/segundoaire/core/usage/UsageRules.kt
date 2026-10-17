package com.segundoaire.core.usage

import com.segundoaire.domain.model.AppUsageSession
import java.time.Instant
import java.time.ZoneId

/** Reglas para cerrar un rato de uso, sin Android, aparte para poder probarlas. */
internal object UsageRules {
    /**
     * Un rato más largo que esto es un hueco anormal (un apagado de pantalla que no llegó,
     * el reloj que saltó): se descarta entero en vez de inflar los minutos.
     */
    const val MAX_SESSION_MILLIS = 3 * 60 * 60_000L

    /** Menos de un segundo es ruido de transición entre ventanas. */
    const val MIN_SESSION_MILLIS = 1_000L

    /** Cada cuánto se corta el rato abierto para volcarlo a Room aunque la app siga abierta. */
    const val FLUSH_INTERVAL_MILLIS = 5 * 60_000L

    /**
     * Cierra el rato de [packageName] entre [startedAt] y [endedAt]. Si cruza la medianoche
     * se parte en un rato por día, así cada día suma lo suyo. Vacío si es anormal: termina
     * antes de empezar, dura menos de [MIN_SESSION_MILLIS] o más de [MAX_SESSION_MILLIS].
     */
    fun close(packageName: String, startedAt: Long, endedAt: Long, zone: ZoneId): List<AppUsageSession> {
        val duration = endedAt - startedAt
        if (duration < MIN_SESSION_MILLIS || duration > MAX_SESSION_MILLIS) return emptyList()
        val sessions = mutableListOf<AppUsageSession>()
        var start = startedAt
        while (start < endedAt) {
            val nextMidnight = Instant.ofEpochMilli(start).atZone(zone).toLocalDate().plusDays(1)
                .atStartOfDay(zone).toInstant().toEpochMilli()
            val end = minOf(endedAt, nextMidnight)
            sessions += AppUsageSession(packageName, start, end)
            start = end
        }
        return sessions
    }
}
