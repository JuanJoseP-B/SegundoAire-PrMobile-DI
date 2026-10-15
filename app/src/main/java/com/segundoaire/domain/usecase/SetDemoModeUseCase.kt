package com.segundoaire.domain.usecase

import com.segundoaire.domain.repository.UserPreferencesRepository
import javax.inject.Inject

/** Activa o desactiva los datos de ejemplo de Progreso. El historial real no se toca. */
class SetDemoModeUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setDemoMode(enabled)
}
