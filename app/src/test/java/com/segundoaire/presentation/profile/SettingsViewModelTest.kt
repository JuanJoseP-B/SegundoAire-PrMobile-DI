package com.segundoaire.presentation.profile

import com.segundoaire.domain.model.AppLanguage
import com.segundoaire.domain.usecase.GetAppLanguageUseCase
import com.segundoaire.domain.usecase.SetAppLanguageUseCase
import com.segundoaire.testing.FakeAppLanguageRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsViewModelTest {

    private fun viewModel(languages: FakeAppLanguageRepository) = SettingsViewModel(
        GetAppLanguageUseCase(languages),
        SetAppLanguageUseCase(languages)
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
}
