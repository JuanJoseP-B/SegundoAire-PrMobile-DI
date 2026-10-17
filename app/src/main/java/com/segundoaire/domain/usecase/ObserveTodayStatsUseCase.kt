package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.domain.model.TodayStats
import com.segundoaire.domain.repository.BlockSessionRepository
import com.segundoaire.domain.repository.InterceptionEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Clock
import javax.inject.Inject

/** Cifras de hoy para el Panel. El día se fija al invocar; el Panel vuelve a pedirlas al cambiar. */
class ObserveTodayStatsUseCase @Inject constructor(
    private val sessions: BlockSessionRepository,
    private val interceptions: InterceptionEventRepository,
    private val clock: Clock
) {
    operator fun invoke(): Flow<TodayStats> {
        val from = progressWindowStart(clock, days = 1)
        return combine(
            sessions.observeCompletedSince(from),
            interceptions.observeSince(from)
        ) { blocks, events ->
            TodayStats(
                recoveredMinutes = blocks.sumOf { it.durationMinutes },
                pausesRespected = events.count { it.result == InterceptionResult.VOLVIO },
                openingsAvoided = events.size
            )
        }
    }
}
