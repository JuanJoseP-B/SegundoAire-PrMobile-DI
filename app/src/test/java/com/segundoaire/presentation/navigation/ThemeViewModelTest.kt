package com.segundoaire.presentation.navigation

import com.segundoaire.domain.model.ThemeMode
import com.segundoaire.domain.repository.UserPreferencesRepository
import com.segundoaire.domain.usecase.ObserveThemeModeUseCase
import com.segundoaire.testing.FakeUserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(preferences: UserPreferencesRepository) =
        ThemeViewModel(ObserveThemeModeUseCase(preferences))

    @Test
    fun `sin tema elegido es el del sistema`() = runTest(dispatcher) {
        val viewModel = viewModel(FakeUserPreferencesRepository())

        assertEquals(ThemeMode.SYSTEM, viewModel.themeMode.value)
    }

    @Test
    fun `arranca con el tema que ya estaba guardado`() = runTest(dispatcher) {
        val viewModel = viewModel(FakeUserPreferencesRepository(themeMode = ThemeMode.DARK))

        assertEquals(ThemeMode.DARK, viewModel.themeMode.value)
    }

    @Test
    fun `cambiar el tema lo aplica al momento`() = runTest(dispatcher) {
        val preferences = FakeUserPreferencesRepository()
        val viewModel = viewModel(preferences)

        preferences.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, viewModel.themeMode.value)

        preferences.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, viewModel.themeMode.value)
    }

    @Test
    fun `si DataStore falla la app sigue al tema del sistema`() = runTest(dispatcher) {
        val viewModel = viewModel(FailingThemePreferences())

        assertEquals(ThemeMode.SYSTEM, viewModel.themeMode.value)
    }

    /** Solo el tema importa aquí: lo demás no se usa. */
    private class FailingThemePreferences : UserPreferencesRepository {
        override val themeMode: Flow<ThemeMode> = flow { throw IOException("archivo dañado") }

        override suspend fun setThemeMode(mode: ThemeMode) = Unit

        override val isOnboardingCompleted: Flow<Boolean> = flowOf(false)

        override suspend fun completeOnboarding() = Unit

        override val plantedAtMillis: Flow<Long?> = flowOf(null)

        override suspend fun markPlanted(atMillis: Long) = Unit

        override val isSoundEnabled: Flow<Boolean> = flowOf(true)

        override suspend fun setSoundEnabled(enabled: Boolean) = Unit

        override val isDemoMode: Flow<Boolean> = flowOf(false)

        override suspend fun setDemoMode(enabled: Boolean) = Unit
    }
}
