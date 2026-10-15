package com.segundoaire.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.segundoaire.data.local.DatabaseCleaner
import com.segundoaire.data.preferences.UserPreferencesRepositoryImpl
import com.segundoaire.data.preferences.UserProfileRepositoryImpl
import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.StartDestination
import com.segundoaire.domain.model.ThemeMode
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.domain.usecase.GetStartDestinationUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

/**
 * El reinicio sobre un DataStore real. Room queda detrás de [DatabaseCleaner]: aquí se
 * comprueba que se pide vaciarlo y en qué orden, no el SQL de `clearAllTables`.
 */
class AppResetRepositoryImplTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val scope = TestScope(UnconfinedTestDispatcher())
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var preferences: UserPreferencesRepositoryImpl
    private lateinit var profiles: UserProfileRepositoryImpl
    private val database = RecordingDatabaseCleaner()

    @Before
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = scope.backgroundScope,
            produceFile = { File(tmp.root, "reset.preferences_pb") }
        )
        preferences = UserPreferencesRepositoryImpl(dataStore)
        profiles = UserProfileRepositoryImpl(dataStore)
    }

    private fun repository() = AppResetRepositoryImpl(dataStore, database)

    private fun startDestination() =
        GetStartDestinationUseCase(preferences, profiles)

    /** Un usuario que ya terminó onboarding, cuenta y setup, con todos los ajustes tocados. */
    private suspend fun seedUsedApp() {
        preferences.completeOnboarding()
        preferences.setThemeMode(ThemeMode.DARK)
        preferences.setDemoMode(true)
        profiles.saveProfile(UserProfile(name = "Ana", goals = listOf(Goal.SLEEP)))
        profiles.saveAccount(AuthMode.EMAIL, "ana@correo.com")
        profiles.completeSetup()
    }

    @Test
    fun `antes de reiniciar la app abre en el Panel`() = scope.runTest {
        seedUsedApp()

        assertEquals(StartDestination.MAIN, startDestination()().first())
    }

    @Test
    fun `reiniciar deja el perfil vacio`() = scope.runTest {
        seedUsedApp()

        repository().clearAll()

        assertEquals(UserProfile(), profiles.profile.first())
    }

    @Test
    fun `reiniciar devuelve el tema, el modo demo y el onboarding a su estado inicial`() = scope.runTest {
        seedUsedApp()

        repository().clearAll()

        assertEquals(ThemeMode.SYSTEM, preferences.themeMode.first())
        assertFalse(preferences.isDemoMode.first())
        assertFalse(preferences.isOnboardingCompleted.first())
    }

    @Test
    fun `tras reiniciar el destino inicial es la bienvenida`() = scope.runTest {
        seedUsedApp()

        repository().clearAll()

        assertEquals(StartDestination.ONBOARDING, startDestination()().first())
    }

    @Test
    fun `no queda ninguna clave en DataStore`() = scope.runTest {
        seedUsedApp()

        repository().clearAll()

        assertTrue(dataStore.data.first().asMap().isEmpty())
    }

    @Test
    fun `vacia Room antes que DataStore`() = scope.runTest {
        seedUsedApp()
        database.onClear = {
            // Si Room falla, el perfil todavía tiene que estar: DataStore es lo último en caer
            assertEquals("Ana", profiles.profile.first().name)
        }

        repository().clearAll()

        assertEquals(1, database.clearCount)
    }

    @Test
    fun `si Room falla el perfil sigue en pie y se puede reintentar`() = scope.runTest {
        seedUsedApp()
        database.failNext = true

        try {
            repository().clearAll()
            fail("Debía propagar el error")
        } catch (_: IOException) {
            // esperado
        }

        assertEquals("Ana", profiles.profile.first().name)
        assertEquals(StartDestination.MAIN, startDestination()().first())

        repository().clearAll()

        assertEquals(StartDestination.ONBOARDING, startDestination()().first())
    }

    private class RecordingDatabaseCleaner : DatabaseCleaner {
        var clearCount = 0
        var failNext = false
        var onClear: suspend () -> Unit = {}

        override suspend fun clearAll() {
            onClear()
            if (failNext) {
                failNext = false
                throw IOException("base dañada")
            }
            clearCount++
        }
    }
}
