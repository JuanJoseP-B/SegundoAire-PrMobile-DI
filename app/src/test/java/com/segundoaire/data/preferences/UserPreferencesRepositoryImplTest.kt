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
}
