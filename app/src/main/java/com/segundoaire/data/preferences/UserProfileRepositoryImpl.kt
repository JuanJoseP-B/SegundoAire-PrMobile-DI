package com.segundoaire.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.RiskMoment
import com.segundoaire.domain.model.UsageRange
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserProfileRepository {

    override val profile: Flow<UserProfile> = dataStore.data.map { prefs ->
        UserProfile(
            name = prefs[NAME].orEmpty(),
            goals = prefs[GOALS].orEmpty()
                .split(LIST_SEPARATOR)
                .mapNotNull { enumOrNull<Goal>(it) },
            usageRange = enumOrNull<UsageRange>(prefs[USAGE_RANGE]),
            riskMoments = prefs[RISK_MOMENTS].orEmpty()
                .mapNotNullTo(mutableSetOf()) { enumOrNull<RiskMoment>(it) },
            suggestedRigor = enumOrNull<RigorLevel>(prefs[SUGGESTED_RIGOR]) ?: RigorLevel.MINDFUL,
            authMode = enumOrNull<AuthMode>(prefs[AUTH_MODE]),
            email = prefs[EMAIL].orEmpty(),
            setupCompleted = prefs[SETUP_COMPLETED] ?: false
        )
    }

    override suspend fun saveProfile(profile: UserProfile) {
        dataStore.edit { prefs ->
            prefs[NAME] = profile.name
            // Como texto y no como stringSet para conservar el orden de elección
            prefs[GOALS] = profile.goals.joinToString(LIST_SEPARATOR) { it.name }
            if (profile.usageRange != null) {
                prefs[USAGE_RANGE] = profile.usageRange.name
            } else {
                prefs.remove(USAGE_RANGE)
            }
            prefs[RISK_MOMENTS] = profile.riskMoments.mapTo(mutableSetOf()) { it.name }
            prefs[SUGGESTED_RIGOR] = profile.suggestedRigor.name
            if (profile.authMode != null) {
                prefs[AUTH_MODE] = profile.authMode.name
            } else {
                prefs.remove(AUTH_MODE)
            }
            prefs[EMAIL] = profile.email
        }
    }

    override suspend fun saveAccount(authMode: AuthMode, email: String) {
        dataStore.edit { prefs ->
            prefs[AUTH_MODE] = authMode.name
            prefs[EMAIL] = email
        }
    }

    override suspend fun completeSetup() {
        dataStore.edit { prefs -> prefs[SETUP_COMPLETED] = true }
    }

    private companion object {
        const val LIST_SEPARATOR = ","
        val NAME = stringPreferencesKey("profile_name")
        val GOALS = stringPreferencesKey("profile_goals")
        val USAGE_RANGE = stringPreferencesKey("profile_usage_range")
        val RISK_MOMENTS = stringSetPreferencesKey("profile_risk_moments")
        val SUGGESTED_RIGOR = stringPreferencesKey("profile_suggested_rigor")
        val AUTH_MODE = stringPreferencesKey("profile_auth_mode")
        val EMAIL = stringPreferencesKey("profile_email")
        val SETUP_COMPLETED = booleanPreferencesKey("profile_setup_completed")

        // Un valor guardado por una versión futura o corrupto se ignora en vez de fallar
        inline fun <reified T : Enum<T>> enumOrNull(name: String?): T? =
            enumValues<T>().firstOrNull { it.name == name }
    }
}
