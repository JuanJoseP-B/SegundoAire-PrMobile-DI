package com.segundoaire.domain.usecase

import com.segundoaire.domain.repository.InterceptionEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

/**
 * Aperturas evitadas: todas las veces que el GlassWall interrumpió una app vigilada
 * en los últimos `days` días de calendario, hoy incluido, se respetara la pausa o no
 * (la misma regla que el Panel). Las que terminaron en "Volver" son las pausas
 * respetadas. La ventana se fija al invocar.
 */
class AperturasEvitadasUseCase @Inject constructor(
    private val repository: InterceptionEventRepository,
    private val clock: Clock
) {
    operator fun invoke(days: Int = PROGRESS_WINDOW_DAYS): Flow<Int> =
        repository.observeSince(progressWindowStart(clock, days))
            .map { events -> events.size }
}
