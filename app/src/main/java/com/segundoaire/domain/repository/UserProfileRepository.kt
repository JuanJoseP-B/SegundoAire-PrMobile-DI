package com.segundoaire.domain.repository

import com.segundoaire.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    val profile: Flow<UserProfile>

    suspend fun saveProfile(profile: UserProfile)
}
