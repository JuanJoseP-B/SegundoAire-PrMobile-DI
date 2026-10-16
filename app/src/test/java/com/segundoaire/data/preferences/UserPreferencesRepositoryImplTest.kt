package com.segundoaire.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.segundoaire.domain.model.ThemeMode
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class UserPreferencesRepositoryImplTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val scope = TestScope(UnconfinedTestDispatcher())
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: UserPreferencesRepositoryImpl

    @Before
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = scope.backgroundScope,
            produceFile = { File(tmp.root, "preferences.preferences_pb") }
        )
        repository = UserPreferencesRepositoryImpl(dataStore)
    }

    @Test
    fun `sin tema elegido sigue al del sistema`() = scope.runTest {
        assertEquals(ThemeMode.SYSTEM, repository.themeMode.first())
    }

    @Test
    fun `guarda y lee cada modo de tema`() = scope.runTest {
        ThemeMode.entries.forEach { mode ->
            repository.setThemeMode(mode)

            assertEquals(mode, repository.themeMode.first())
        }
    }

    @Test
    fun `volver a Sistema reemplaza el tema elegido`() = scope.runTest {
        repository.setThemeMode(ThemeMode.DARK)
        repository.setThemeMode(ThemeMode.SYSTEM)

        assertEquals(ThemeMode.SYSTEM, repository.themeMode.first())
    }

    @Test
    fun `un valor desconocido se lee como Sistema`() = scope.runTest {
        // Lo que dejaría una versión futura con un tema nuevo, o un archivo dañado
        dataStore.edit { it[stringPreferencesKey("theme_mode")] = "SEPIA" }

        assertEquals(ThemeMode.SYSTEM, repository.themeMode.first())
    }

    @Test
    fun `el tema no toca el onboarding ni el modo demo`() = scope.runTest {
        repository.completeOnboarding()
        repository.setDemoMode(true)

        repository.setThemeMode(ThemeMode.LIGHT)

        assertEquals(true, repository.isOnboardingCompleted.first())
        assertEquals(true, repository.isDemoMode.first())
    }

    @Test
    fun `el modo demo arranca apagado`() = scope.runTest {
        assertFalse(repository.isDemoMode.first())
    }

    @Test
    fun `el modo demo se guarda y se puede apagar`() = scope.runTest {
        repository.setDemoMode(true)
        assertTrue(repository.isDemoMode.first())

        repository.setDemoMode(false)
        assertFalse(repository.isDemoMode.first())
    }

    @Test
    fun `el modo demo no toca el tema`() = scope.runTest {
        repository.setThemeMode(ThemeMode.LIGHT)

        repository.setDemoMode(true)

        assertEquals(ThemeMode.LIGHT, repository.themeMode.first())
    }

    @Test
    fun `sin onboarding no hay fecha de plantado`() = scope.runTest {
        assertEquals(null, repository.plantedAtMillis.first())
    }

    @Test
    fun `la fecha de plantado se guarda una sola vez`() = scope.runTest {
        repository.markPlanted(1_000L)
        repository.markPlanted(2_000L)

        assertEquals(1_000L, repository.plantedAtMillis.first())
    }
}
