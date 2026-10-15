package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.ThemeMode
import com.segundoaire.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Tema elegido en Ajustes; [ThemeMode.SYSTEM] mientras no se haya elegido otro. */
class ObserveThemeModeUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<ThemeMode> = repository.themeMode
}
