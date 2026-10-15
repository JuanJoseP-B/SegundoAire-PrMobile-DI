package com.segundoaire.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.model.ThemeMode
import com.segundoaire.domain.usecase.ObserveThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Tema de la app para `MainActivity`. Cambiarlo en Ajustes recompone el tema en
 * el acto: la actividad no se reinicia.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    observeThemeMode: ObserveThemeModeUseCase
) : ViewModel() {

    /** `null` mientras se lee DataStore, para no pintar un tema que luego cambie. */
    val themeMode: StateFlow<ThemeMode?> = observeThemeMode()
        // Si DataStore falla, la app sigue al tema del sistema
        .catch { emit(ThemeMode.SYSTEM) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
