package com.segundoaire.domain.usecase

import java.time.Clock
import java.time.LocalDate

/** Ventana por defecto de las estadísticas de Progreso: la semana que termina hoy. */
const val PROGRESS_WINDOW_DAYS = 7

/**
 * Inicio (ms desde epoch) de los últimos [days] días de calendario, hoy incluido,
 * en la zona de [clock]. Con `days = 1` es la medianoche de hoy.
 */
internal fun progressWindowStart(clock: Clock, days: Int): Long {
    require(days >= 1) { "La ventana debe cubrir al menos un día: $days" }
    return LocalDate.now(clock)
        .minusDays(days - 1L)
        .atStartOfDay(clock.zone)
        .toInstant()
        .toEpochMilli()
}
