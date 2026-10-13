package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.StartDestination
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.domain.repository.UserPreferencesRepository
import com.segundoaire.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Decide el destino inicial: sin onboarding → onboarding; sin cuenta → cuenta;
 * sin setup → permisos y apps; si no, el Panel.
 */
class GetStartDestinationUseCase @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val profiles: UserProfileRepository
) {
    operator fun invoke(): Flow<StartDestination> =
        combine(preferences.isOnboardingCompleted, profiles.profile, ::resolve)

    companion object {
        fun resolve(isOnboardingCompleted: Boolean, profile: UserProfile): StartDestination = when {
            !isOnboardingCompleted -> StartDestination.ONBOARDING
            profile.authMode == null -> StartDestination.ACCOUNT
            !profile.setupCompleted -> StartDestination.SETUP
            else -> StartDestination.MAIN
        }
    }
}
