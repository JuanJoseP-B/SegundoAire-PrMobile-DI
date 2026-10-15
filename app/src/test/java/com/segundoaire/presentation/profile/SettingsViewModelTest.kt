package com.segundoaire.presentation.profile

import com.segundoaire.domain.model.AppLanguage
import com.segundoaire.domain.model.ThemeMode
import com.segundoaire.domain.usecase.GetAppLanguageUseCase
import com.segundoaire.domain.usecase.ObserveDemoModeUseCase
import com.segundoaire.domain.usecase.ObserveThemeModeUseCase
import com.segundoaire.domain.usecase.ResetAppUseCase
import com.segundoaire.domain.usecase.SetAppLanguageUseCase
import com.segundoaire.domain.usecase.SetDemoModeUseCase
import com.segundoaire.domain.usecase.SetThemeModeUseCase
import com.segundoaire.testing.FakeAppLanguageRepository
import com.segundoaire.testing.FakeAppResetRepository
import com.segundoaire.testing.FakeBlockRepository
import com.segundoaire.testing.FakeUserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Clock
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val preferences = FakeUserPreferencesRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val appReset = FakeAppResetRepository()
    private val blocks = FakeBlockRepository(Clock.systemUTC())

    private fun viewModel(
        languages: FakeAppLanguageRepository,
        prefs: FakeUserPreferencesRepository = preferences
    ) = SettingsViewModel(
        GetAppLanguageUseCase(languages),
        SetAppLanguageUseCase(languages),
        ObserveThemeModeUseCase(prefs),
        SetThemeModeUseCase(prefs),
        ObserveDemoModeUseCase(prefs),
        SetDemoModeUseCase(prefs),
        ResetAppUseCase(appReset, blocks, languages)
    )

    @Test
    fun `sin idioma elegido sigue al del sistema`() {
        val viewModel = viewModel(FakeAppLanguageRepository())

        assertEquals(AppLanguage.SYSTEM, viewModel.uiState.value.language)
    }

    @Test
    fun `arranca con el idioma que ya estaba elegido`() {
        val viewModel = viewModel(FakeAppLanguageRepository(AppLanguage.ENGLISH))

        assertEquals(AppLanguage.ENGLISH, viewModel.uiState.value.language)
    }

    @Test
    fun `elegir un idioma lo aplica y lo muestra`() {
        val languages = FakeAppLanguageRepository()
        val viewModel = viewModel(languages)

        viewModel.onLanguageSelected(AppLanguage.SPANISH)

        assertEquals(AppLanguage.SPANISH, languages.current())
        assertEquals(AppLanguage.SPANISH, viewModel.uiState.value.language)
    }

    @Test
    fun `volver a Sistema quita el idioma elegido`() {
        val languages = FakeAppLanguageRepository(AppLanguage.ENGLISH)
        val viewModel = viewModel(languages)

        viewModel.onLanguageSelected(AppLanguage.SYSTEM)

        assertEquals(AppLanguage.SYSTEM, languages.current())
        assertEquals(AppLanguage.SYSTEM, viewModel.uiState.value.language)
    }

    @Test
    fun `elegir el idioma que ya está no lo vuelve a aplicar`() {
        // Aplicar un idioma recrea la actividad: hacerlo sin cambio parpadearía la pantalla
        val languages = FakeAppLanguageRepository(AppLanguage.SPANISH)
        val viewModel = viewModel(languages)

        viewModel.onLanguageSelected(AppLanguage.SPANISH)

        assertEquals(0, languages.setCount)
    }

    @Test
    fun `al volver a la app refleja un cambio hecho en los ajustes del sistema`() {
        val languages = FakeAppLanguageRepository()
        val viewModel = viewModel(languages)

        languages.changeExternally(AppLanguage.ENGLISH)
        viewModel.onResume()

        assertEquals(AppLanguage.ENGLISH, viewModel.uiState.value.language)
    }

    @Test
    fun `sin tema elegido el selector marca Sistema`() {
        val viewModel = viewModel(FakeAppLanguageRepository())

        assertEquals(ThemeMode.SYSTEM, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `arranca con el tema que ya estaba guardado`() {
        val saved = FakeUserPreferencesRepository(themeMode = ThemeMode.DARK)
        val viewModel = viewModel(FakeAppLanguageRepository(), saved)

        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `elegir un tema lo guarda y el selector lo refleja`() = runTest {
        val viewModel = viewModel(FakeAppLanguageRepository())

        viewModel.onThemeModeSelected(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, preferences.themeMode.first())
        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `cambiar el tema no toca el idioma`() = runTest {
        val languages = FakeAppLanguageRepository(AppLanguage.ENGLISH)
        val viewModel = viewModel(languages)

        viewModel.onThemeModeSelected(ThemeMode.LIGHT)

        assertEquals(AppLanguage.ENGLISH, languages.current())
        assertEquals(AppLanguage.ENGLISH, viewModel.uiState.value.language)
        assertEquals(0, languages.setCount)
    }

    @Test
    fun `volver a Sistema reemplaza el tema elegido`() = runTest {
        val viewModel = viewModel(FakeAppLanguageRepository())
        viewModel.onThemeModeSelected(ThemeMode.DARK)

        viewModel.onThemeModeSelected(ThemeMode.SYSTEM)

        assertEquals(ThemeMode.SYSTEM, preferences.themeMode.first())
        assertEquals(ThemeMode.SYSTEM, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `el modo demo arranca apagado`() {
        val viewModel = viewModel(FakeAppLanguageRepository())

        assertFalse(viewModel.uiState.value.isDemoMode)
    }

    @Test
    fun `arranca con el modo demo que ya estaba guardado`() {
        val viewModel = viewModel(FakeAppLanguageRepository(), FakeUserPreferencesRepository(demoMode = true))

        assertTrue(viewModel.uiState.value.isDemoMode)
    }

    @Test
    fun `activar el modo demo lo guarda y el interruptor lo refleja`() = runTest {
        val viewModel = viewModel(FakeAppLanguageRepository())

        viewModel.onDemoModeChange(true)

        assertTrue(preferences.isDemoMode.first())
        assertTrue(viewModel.uiState.value.isDemoMode)
    }

    @Test
    fun `apagar el modo demo vuelve a los datos reales`() = runTest {
        val viewModel = viewModel(FakeAppLanguageRepository(), FakeUserPreferencesRepository(demoMode = true))

        viewModel.onDemoModeChange(false)

        assertFalse(viewModel.uiState.value.isDemoMode)
    }

    @Test
    fun `el modo demo no toca el tema ni el idioma`() = runTest {
        val languages = FakeAppLanguageRepository(AppLanguage.SPANISH)
        val viewModel = viewModel(languages)
        viewModel.onThemeModeSelected(ThemeMode.DARK)

        viewModel.onDemoModeChange(true)

        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
        assertEquals(AppLanguage.SPANISH, viewModel.uiState.value.language)
    }

    @Test
    fun `reiniciar borra los datos y avisa para volver a la bienvenida`() = runTest {
        val viewModel = viewModel(FakeAppLanguageRepository())

        viewModel.onResetConfirmed()

        val state = viewModel.uiState.value
        assertEquals(1, appReset.clearCount)
        assertTrue(state.isResetCompleted)
        assertFalse(state.isResetting)
        assertFalse(state.resetFailed)
    }

    @Test
    fun `reiniciar vuelve al idioma del sistema`() = runTest {
        val languages = FakeAppLanguageRepository(AppLanguage.ENGLISH)
        val viewModel = viewModel(languages)

        viewModel.onResetConfirmed()

        assertEquals(AppLanguage.SYSTEM, languages.current())
    }

    @Test
    fun `si el reinicio falla no se completa y se puede reintentar`() = runTest {
        val viewModel = viewModel(FakeAppLanguageRepository())
        appReset.failNext = true

        viewModel.onResetConfirmed()

        val failed = viewModel.uiState.value
        assertTrue(failed.resetFailed)
        assertFalse(failed.isResetCompleted)
        assertFalse(failed.isResetting)

        viewModel.onResetConfirmed()

        val retried = viewModel.uiState.value
        assertTrue(retried.isResetCompleted)
        assertFalse(retried.resetFailed)
    }

    @Test
    fun `un reinicio ya completado no se repite`() = runTest {
        val viewModel = viewModel(FakeAppLanguageRepository())
        viewModel.onResetConfirmed()

        viewModel.onResetConfirmed()

        assertEquals(1, appReset.clearCount)
    }
}
