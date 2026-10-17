package com.segundoaire.domain.usecase

import com.segundoaire.domain.repository.BlockSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

/**
 * La racha más larga de todo el historial: días seguidos con al menos un bloque
 * completado. Un bloque cuenta el día en que terminó, en la zona del reloj.
 */
class MejorRachaUseCase @Inject constructor(
    private val repository: BlockSessionRepository,
    private val clock: Clock
) {
    operator fun invoke(): Flow<Int> =
        repository.observeCompletedSince(fromMillis = 0L).map { sessions ->
            longestStreak(sessions.mapTo(HashSet()) { Instant.ofEpochMilli(it.endedAt).atZone(clock.zone).toLocalDate() })
        }

    companion object {
        /** Días consecutivos más largos dentro de [days]. */
        fun longestStreak(days: Set<LocalDate>): Int {
            var best = 0
            days.forEach { day ->
                // Solo se cuenta desde el primer día de cada tramo
                if (day.minusDays(1) in days) return@forEach
                var length = 1
                while (day.plusDays(length.toLong()) in days) length++
                best = maxOf(best, length)
            }
            return best
        }
    }
}
