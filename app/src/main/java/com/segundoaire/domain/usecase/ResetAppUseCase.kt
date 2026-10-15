package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.AppLanguage
import com.segundoaire.domain.repository.AppLanguageRepository
import com.segundoaire.domain.repository.AppResetRepository
import javax.inject.Inject

/**
 * Devuelve la app al estado de recién instalada: borra los datos locales y
 * vuelve al idioma del sistema. Los permisos del sistema no se tocan: Android
 * no permite quitarlos desde la app.
 */
class ResetAppUseCase @Inject constructor(
    private val appReset: AppResetRepository,
    private val language: AppLanguageRepository
) {
    suspend operator fun invoke() {
        appReset.clearAll()
        // Al final: cambiar el idioma vuelve a crear la actividad
        if (language.current() != AppLanguage.SYSTEM) language.set(AppLanguage.SYSTEM)
    }
}
