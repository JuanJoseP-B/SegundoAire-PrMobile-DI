package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.InterceptionEvent
import com.segundoaire.domain.repository.InterceptionEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/** La hora del día (0–23) con más intercepciones en los últimos `days` días, o `null` sin ninguna. */
class HoraMasDificilUseCase @Inject constructor(
    private val repository: InterceptionEventRepository,
    private val clock: Clock
) {
    operator fun invoke(days: Int = PROGRESS_WINDOW_DAYS): Flow<Int?> =
        repository.observeSince(progressWindowStart(clock, days))
            .map { events -> hardestHour(events, clock.zone)?.first }

    companion object {
        /**
         * Hora con más intercepciones y cuántas fueron. En empate gana la más tardía:
         * suele ser la de la noche, la que más cuesta.
         */
        fun hardestHour(events: List<InterceptionEvent>, zone: ZoneId): Pair<Int, Int>? = events
            .groupingBy { Instant.ofEpochMilli(it.timestampMillis).atZone(zone).hour }
            .eachCount()
            .maxWithOrNull(compareBy<Map.Entry<Int, Int>> { it.value }.thenBy { it.key })
            ?.let { it.key to it.value }
    }
}
