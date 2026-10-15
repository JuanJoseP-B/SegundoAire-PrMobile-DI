package com.segundoaire.presentation.profile

import androidx.lifecycle.ViewModel
import com.segundoaire.domain.model.AppLanguage
import com.segundoaire.domain.usecase.GetAppLanguageUseCase
import com.segundoaire.domain.usecase.SetAppLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Ajustes de la app. Cada cambio se aplica al momento y no hay botón de guardar:
 * el idioma lo conserva el sistema, no la app.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getAppLanguage: GetAppLanguageUseCase,
    private val setAppLanguage: SetAppLanguageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(language = getAppLanguage()))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onLanguageSelected(language: AppLanguage) {
        if (language == _uiState.value.language) return
        setAppLanguage(language)
        _uiState.update { it.copy(language = language) }
    }

    /** Al volver a la pantalla (ON_RESUME): el idioma pudo cambiarse en los ajustes del sistema. */
    fun onResume() {
        _uiState.update { it.copy(language = getAppLanguage()) }
    }
}
