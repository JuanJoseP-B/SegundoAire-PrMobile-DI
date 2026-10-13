package com.segundoaire.testing

import com.segundoaire.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Preferencias en memoria; el test marca el onboarding directamente o con [completeOnboarding]. */
class FakeUserPreferencesRepository(onboardingCompleted: Boolean = false) : UserPreferencesRepository {
    private val completed = MutableStateFlow(onboardingCompleted)
    override val isOnboardingCompleted: Flow<Boolean> = completed

    override suspend fun completeOnboarding() {
        completed.value = true
    }
}
