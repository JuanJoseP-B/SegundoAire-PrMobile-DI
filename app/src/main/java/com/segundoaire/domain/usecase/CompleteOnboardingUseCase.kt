package com.segundoaire.domain.usecase

import com.segundoaire.domain.repository.UserPreferencesRepository
import java.time.Clock
import javax.inject.Inject

/** Cierra el onboarding y planta el árbol: guarda el día que muestra el Perfil. */
class CompleteOnboardingUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
    private val clock: Clock
) {
    suspend operator fun invoke() {
        repository.markPlanted(clock.millis())
        repository.completeOnboarding()
    }
}
