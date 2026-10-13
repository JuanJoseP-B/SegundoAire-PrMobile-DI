package com.segundoaire.testing

import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException

/** Repositorio en memoria. Con [failNextSave] el siguiente guardado falla como lo haría DataStore. */
class FakeUserProfileRepository : UserProfileRepository {
    var saved: UserProfile? = null
    var saveCount = 0
    var failNextSave = false

    private val current = MutableStateFlow(UserProfile())
    override val profile: Flow<UserProfile> = current

    // Como el repositorio real, saveProfile no escribe setupCompleted
    override suspend fun saveProfile(profile: UserProfile) =
        save(profile.copy(setupCompleted = current.value.setupCompleted))

    override suspend fun saveAccount(authMode: AuthMode, email: String) =
        save(current.value.copy(authMode = authMode, email = email))

    override suspend fun completeSetup() = save(current.value.copy(setupCompleted = true))

    private fun save(profile: UserProfile) {
        if (failNextSave) {
            failNextSave = false
            throw IOException("disco lleno")
        }
        current.value = profile
        saved = profile
        saveCount++
    }
}
