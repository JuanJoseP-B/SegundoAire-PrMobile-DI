package com.segundoaire.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.RiskMoment
import com.segundoaire.domain.model.UsageRange
import com.segundoaire.domain.model.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class UserProfileRepositoryImplTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val scope = TestScope(UnconfinedTestDispatcher())
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: UserProfileRepositoryImpl

    @Before
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = scope.backgroundScope,
            produceFile = { File(tmp.root, "profile.preferences_pb") }
        )
        repository = UserProfileRepositoryImpl(dataStore)
    }

    @Test
    fun `sin datos guardados devuelve el perfil vacio`() = scope.runTest {
        assertEquals(UserProfile(), repository.profile.first())
    }

    @Test
    fun `guarda y lee el perfil completo`() = scope.runTest {
        val profile = UserProfile(
            name = "Ana",
            goals = listOf(Goal.SLEEP, Goal.FOCUS),
            usageRange = UsageRange.FROM_6_TO_8,
            riskMoments = setOf(RiskMoment.BEFORE_SLEEP, RiskMoment.BORED),
            suggestedRigor = RigorLevel.STRICT
        )

        repository.saveProfile(profile)

        assertEquals(profile, repository.profile.first())
    }

    @Test
    fun `conserva el orden en que se eligieron los objetivos`() = scope.runTest {
        repository.saveProfile(UserProfile(goals = listOf(Goal.FREE_TIME, Goal.FOCUS)))

        assertEquals(listOf(Goal.FREE_TIME, Goal.FOCUS), repository.profile.first().goals)
    }

    @Test
    fun `guardar de nuevo reemplaza el perfil anterior`() = scope.runTest {
        repository.saveProfile(
            UserProfile(
                name = "Ana",
                goals = listOf(Goal.SLEEP),
                usageRange = UsageRange.OVER_8,
                riskMoments = setOf(RiskMoment.BORED)
            )
        )

        repository.saveProfile(UserProfile(name = "Luis"))

        assertEquals(UserProfile(name = "Luis"), repository.profile.first())
    }

    @Test
    fun `guarda y lee la cuenta junto al perfil`() = scope.runTest {
        val profile = UserProfile(name = "Ana", authMode = AuthMode.EMAIL, email = "ana@correo.com")

        repository.saveProfile(profile)

        assertEquals(profile, repository.profile.first())
    }

    @Test
    fun `guardar la cuenta no toca el resto del perfil`() = scope.runTest {
        val profile = UserProfile(
            name = "Ana",
            goals = listOf(Goal.SLEEP),
            usageRange = UsageRange.FROM_2_TO_4,
            riskMoments = setOf(RiskMoment.BORED),
            suggestedRigor = RigorLevel.STRICT
        )
        repository.saveProfile(profile)

        repository.saveAccount(AuthMode.GOOGLE, email = "")

        assertEquals(profile.copy(authMode = AuthMode.GOOGLE), repository.profile.first())
    }

    @Test
    fun `cambiar de cuenta reemplaza la anterior`() = scope.runTest {
        repository.saveAccount(AuthMode.EMAIL, email = "ana@correo.com")

        repository.saveAccount(AuthMode.GUEST, email = "")

        assertEquals(UserProfile(authMode = AuthMode.GUEST), repository.profile.first())
    }

    @Test
    fun `ignora valores que no reconoce`() = scope.runTest {
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("profile_auth_mode")] = "FACEBOOK"
            prefs[stringPreferencesKey("profile_goals")] = "SLEEP,VOLAR"
            prefs[stringPreferencesKey("profile_usage_range")] = "OTRO"
            prefs[stringSetPreferencesKey("profile_risk_moments")] = setOf("BORED", "NUNCA")
            prefs[stringPreferencesKey("profile_suggested_rigor")] = "EXTREMO"
        }

        assertEquals(
            UserProfile(goals = listOf(Goal.SLEEP), riskMoments = setOf(RiskMoment.BORED)),
            repository.profile.first()
        )
    }
}
