package com.segundoaire.domain.repository

import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    val profile: Flow<UserProfile>

    suspend fun saveProfile(profile: UserProfile)

    /** Guarda solo la cuenta, sin tocar el resto del perfil. */
    suspend fun saveAccount(authMode: AuthMode, email: String)

    /** Marca el setup como completo. [saveProfile] no lo toca, así que repetir el onboarding no lo reinicia. */
    suspend fun completeSetup()
}
