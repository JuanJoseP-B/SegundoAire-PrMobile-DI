package com.segundoaire.domain.usecase

import com.segundoaire.domain.repository.BlockSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

/**
 * Días seguidos con al menos un bloque completado, contando hacia atrás desde hoy
 * en la zona del reloj. Un bloque cuenta el día en que terminó.
 *
 * Si hoy todavía no hay ninguno, la racha se cuenta desde ayer: se rompe cuando
 * pasa un día entero sin bloques, no al empezar el siguiente. "Hoy" se lee del
 * reloj en cada emisión.
 */
class RachaUseCase @Inject constructor(
    private val repository: BlockSessionRepository,
    private val clock: Clock
) {
    operator fun invoke(): Flow<Int> =
        repository.observeCompletedSince(fromMillis = 0L).map { sessions ->
            val zone = clock.zone
            val daysWithBlock = sessions.mapTo(HashSet()) {
                Instant.ofEpochMilli(it.endedAt).atZone(zone).toLocalDate()
            }
            val today = LocalDate.now(clock)
            var day = if (today in daysWithBlock) today else today.minusDays(1)
            var streak = 0
            while (day in daysWithBlock) {
                streak++
                day = day.minusDays(1)
            }
            streak
        }
}
