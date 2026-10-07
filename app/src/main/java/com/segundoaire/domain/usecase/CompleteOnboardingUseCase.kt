package com.segundoaire.domain.usecase

import com.segundoaire.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke() = repository.completeOnboarding()
}
