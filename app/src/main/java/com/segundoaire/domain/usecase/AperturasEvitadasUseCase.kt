package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.domain.repository.InterceptionEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

/**
 * Intercepciones en las que el usuario volvió, en los últimos `days` días de
 * calendario, hoy incluido. Las que siguen abiertas o sin resultado no cuentan.
 * La ventana se fija al invocar.
 */
class AperturasEvitadasUseCase @Inject constructor(
    private val repository: InterceptionEventRepository,
    private val clock: Clock
) {
    operator fun invoke(days: Int = PROGRESS_WINDOW_DAYS): Flow<Int> =
        repository.observeSince(progressWindowStart(clock, days))
            .map { events -> events.count { it.result == InterceptionResult.VOLVIO } }
}
