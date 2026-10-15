package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.AppLanguage
import com.segundoaire.domain.repository.AppLanguageRepository
import com.segundoaire.domain.repository.AppResetRepository
import com.segundoaire.domain.repository.BlockRepository
import javax.inject.Inject

/**
 * Devuelve la app al estado de recién instalada: borra los datos locales y
 * vuelve al idioma del sistema. Los permisos del sistema no se tocan: Android
 * no permite quitarlos desde la app.
 *
 * El bloque activo vive también en memoria, donde lo lee el motor: se olvida
 * para que un bloque Strict en curso no sobreviva al reinicio.
 */
class ResetAppUseCase @Inject constructor(
    private val appReset: AppResetRepository,
    private val blocks: BlockRepository,
    private val language: AppLanguageRepository
) {
    suspend operator fun invoke() {
        appReset.clearAll()
        blocks.forget()
        // Al final: cambiar el idioma vuelve a crear la actividad
        if (language.current() != AppLanguage.SYSTEM) language.set(AppLanguage.SYSTEM)
    }
}
