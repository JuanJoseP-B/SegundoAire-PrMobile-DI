package com.segundoaire.presentation.profile

import com.segundoaire.domain.model.AppLanguage
import com.segundoaire.domain.model.ThemeMode
import com.segundoaire.domain.usecase.GetAppLanguageUseCase
import com.segundoaire.domain.usecase.ObserveThemeModeUseCase
import com.segundoaire.domain.usecase.SetAppLanguageUseCase
import com.segundoaire.domain.usecase.SetThemeModeUseCase
import com.segundoaire.testing.FakeAppLanguageRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val preferences = FakeUserPreferencesRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(languages: FakeAppLanguageRepository) = SettingsViewModel(
        GetAppLanguageUseCase(languages),
        SetAppLanguageUseCase(languages),
        ObserveThemeModeUseCase(preferences),
        SetThemeModeUseCase(preferences)
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
        val viewModel = SettingsViewModel(
            GetAppLanguageUseCase(FakeAppLanguageRepository()),
            SetAppLanguageUseCase(FakeAppLanguageRepository()),
            ObserveThemeModeUseCase(saved),
            SetThemeModeUseCase(saved)
        )

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
}
