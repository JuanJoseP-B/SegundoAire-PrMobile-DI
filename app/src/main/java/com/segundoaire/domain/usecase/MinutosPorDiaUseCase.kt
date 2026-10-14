package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.DayMinutes
import com.segundoaire.domain.repository.BlockSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

/**
 * Minutos recuperados de cada uno de los últimos `days` días de calendario, del más
 * antiguo a hoy. Siempre hay un elemento por día, también los días sin bloques. Un
 * bloque cuenta el día en que terminó. La ventana se fija al invocar.
 */
class MinutosPorDiaUseCase @Inject constructor(
    private val repository: BlockSessionRepository,
    private val clock: Clock
) {
    operator fun invoke(days: Int = PROGRESS_WINDOW_DAYS): Flow<List<DayMinutes>> {
        val from = progressWindowStart(clock, days)
        val zone = clock.zone
        val firstDay = Instant.ofEpochMilli(from).atZone(zone).toLocalDate()
        val window: List<LocalDate> = (0 until days).map { firstDay.plusDays(it.toLong()) }
        return repository.observeCompletedSince(from).map { sessions ->
            val byDay = sessions.groupingBy { Instant.ofEpochMilli(it.endedAt).atZone(zone).toLocalDate() }
                .fold(0) { total, session -> total + session.durationMinutes }
            window.map { day -> DayMinutes(day, byDay[day] ?: 0) }
        }
    }
}
