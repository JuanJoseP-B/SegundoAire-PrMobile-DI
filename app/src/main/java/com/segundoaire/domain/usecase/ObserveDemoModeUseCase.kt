package com.segundoaire.domain.usecase

import com.segundoaire.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** `true` mientras Progreso muestra datos de ejemplo. */
class ObserveDemoModeUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.isDemoMode
}
