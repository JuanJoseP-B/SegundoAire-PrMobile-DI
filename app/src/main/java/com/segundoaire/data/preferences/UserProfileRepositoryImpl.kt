package com.segundoaire.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
            suggestedRigor = enumOrNull<RigorLevel>(prefs[SUGGESTED_RIGOR]) ?: RigorLevel.MINDFUL
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
        }
    }

    private companion object {
        const val LIST_SEPARATOR = ","
        val NAME = stringPreferencesKey("profile_name")
        val GOALS = stringPreferencesKey("profile_goals")
        val USAGE_RANGE = stringPreferencesKey("profile_usage_range")
        val RISK_MOMENTS = stringSetPreferencesKey("profile_risk_moments")
        val SUGGESTED_RIGOR = stringPreferencesKey("profile_suggested_rigor")

        // Un valor guardado por una versión futura o corrupto se ignora en vez de fallar
        inline fun <reified T : Enum<T>> enumOrNull(name: String?): T? =
            enumValues<T>().firstOrNull { it.name == name }
    }
}
