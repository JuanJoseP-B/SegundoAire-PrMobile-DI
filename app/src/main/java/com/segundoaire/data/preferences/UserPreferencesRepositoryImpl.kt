package com.segundoaire.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.segundoaire.domain.model.ThemeMode
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

    // Un valor guardado por una versión futura o corrupto cae en el tema del sistema
    override val themeMode: Flow<ThemeMode> = dataStore.data
        .map { prefs -> ThemeMode.entries.firstOrNull { it.name == prefs[THEME_MODE] } ?: ThemeMode.SYSTEM }
        .distinctUntilChanged()

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[THEME_MODE] = mode.name }
    }

    private companion object {
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val IS_DEMO_MODE = booleanPreferencesKey("is_demo_mode")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
