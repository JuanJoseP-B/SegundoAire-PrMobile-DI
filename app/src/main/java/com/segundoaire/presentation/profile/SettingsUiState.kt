package com.segundoaire.presentation.profile

import androidx.compose.runtime.Immutable
import com.segundoaire.domain.model.AppLanguage

/** Estado de los ajustes de la app que se aplican al momento. */
@Immutable
data class SettingsUiState(
    val language: AppLanguage = AppLanguage.SYSTEM
)
