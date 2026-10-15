package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.ThemeMode
import com.segundoaire.domain.repository.UserPreferencesRepository
import javax.inject.Inject

/** Guarda el tema elegido; quien lo observa lo aplica sin reiniciar la actividad. */
class SetThemeModeUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(mode: ThemeMode) = repository.setThemeMode(mode)
}
