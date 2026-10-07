package com.segundoaire.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isOnboardingCompleted: Flow<Boolean>

    suspend fun completeOnboarding()
}
