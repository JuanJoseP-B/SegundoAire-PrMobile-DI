package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.UserProfile
import com.segundoaire.domain.repository.UserProfileRepository
import javax.inject.Inject

/** Guarda el perfil ya normalizado: nombre sin espacios sobrantes y objetivos dentro del límite. */
class SaveUserProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile) = repository.saveProfile(
        profile.copy(
            name = profile.name.trim().take(UserProfile.MAX_NAME_LENGTH),
            goals = profile.goals.distinct().take(UserProfile.MAX_GOALS)
        )
    )
}
