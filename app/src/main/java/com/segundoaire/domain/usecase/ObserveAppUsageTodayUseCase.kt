package com.segundoaire.domain.usecase

import com.segundoaire.domain.repository.AppUsageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

/** Minutos estimados de hoy por app vigilada (paquete → minutos). El día se fija al invocar. */
class ObserveAppUsageTodayUseCase @Inject constructor(
    private val repository: AppUsageRepository,
    private val clock: Clock
) {
    operator fun invoke(): Flow<Map<String, Int>> =
        repository.observeUsageSince(progressWindowStart(clock, days = 1))
            .map { usage -> usage.mapValues { (_, millis) -> (millis / MILLIS_PER_MINUTE).toInt() } }

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
    }
}
