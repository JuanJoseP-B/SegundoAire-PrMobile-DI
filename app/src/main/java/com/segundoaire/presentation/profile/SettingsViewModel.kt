package com.segundoaire.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.model.AppLanguage
import com.segundoaire.domain.model.ThemeMode
import com.segundoaire.domain.usecase.GetAppLanguageUseCase
import com.segundoaire.domain.usecase.ObserveThemeModeUseCase
import com.segundoaire.domain.usecase.SetAppLanguageUseCase
import com.segundoaire.domain.usecase.SetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

/**
 * Ajustes de la app. Cada cambio se aplica al momento y no hay botón de guardar.
 * El idioma lo conserva el sistema; el tema, DataStore, y el selector lo muestra
 * cuando DataStore lo confirma, para no enseñar uno que no quedó guardado.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getAppLanguage: GetAppLanguageUseCase,
    private val setAppLanguage: SetAppLanguageUseCase,
    observeThemeMode: ObserveThemeModeUseCase,
    private val setThemeMode: SetThemeModeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(language = getAppLanguage()))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeThemeMode()
                // Si DataStore falla, el selector queda en Sistema, que es el tema que se aplica
                .catch { emit(ThemeMode.SYSTEM) }
                .collect { mode -> _uiState.update { it.copy(themeMode = mode) } }
        }
    }

    fun onLanguageSelected(language: AppLanguage) {
        if (language == _uiState.value.language) return
        setAppLanguage(language)
        _uiState.update { it.copy(language = language) }
    }

    fun onThemeModeSelected(mode: ThemeMode) {
        if (mode == _uiState.value.themeMode) return
        viewModelScope.launch {
            try {
                setThemeMode(mode)
            } catch (_: IOException) {
                // No se pudo guardar: el selector sigue mostrando el tema que de verdad se aplica
            }
        }
    }

    /** Al volver a la pantalla (ON_RESUME): el idioma pudo cambiarse en los ajustes del sistema. */
    fun onResume() {
        _uiState.update { it.copy(language = getAppLanguage()) }
    }
}
