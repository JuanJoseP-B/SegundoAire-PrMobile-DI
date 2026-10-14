package com.segundoaire.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.segundoaire.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    override val isOnboardingCompleted: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[IS_ONBOARDING_COMPLETED] ?: false }

    override suspend fun completeOnboarding() {
        dataStore.edit { prefs -> prefs[IS_ONBOARDING_COMPLETED] = true }
    }

    override val isDemoMode: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[IS_DEMO_MODE] ?: false }
        .distinctUntilChanged()

    override suspend fun setDemoMode(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[IS_DEMO_MODE] = enabled }
    }

    private companion object {
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val IS_DEMO_MODE = booleanPreferencesKey("is_demo_mode")
    }
}
