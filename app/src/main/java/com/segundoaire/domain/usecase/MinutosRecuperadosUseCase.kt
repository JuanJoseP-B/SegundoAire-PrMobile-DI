package com.segundoaire.domain.usecase

import com.segundoaire.domain.repository.BlockSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

/**
 * Minutos de los bloques completados en los últimos `days` días de calendario,
 * hoy incluido. Un bloque cuenta el día en que terminó; los detenidos antes de
 * tiempo no suman. La ventana se fija al invocar.
 */
class MinutosRecuperadosUseCase @Inject constructor(
    private val repository: BlockSessionRepository,
    private val clock: Clock
) {
    operator fun invoke(days: Int = PROGRESS_WINDOW_DAYS): Flow<Int> =
        repository.observeCompletedSince(progressWindowStart(clock, days))
            .map { sessions -> sessions.sumOf { it.durationMinutes } }
}
