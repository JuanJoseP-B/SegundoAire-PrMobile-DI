package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.AppLanguage
import com.segundoaire.domain.repository.AppLanguageRepository
import javax.inject.Inject

/** Cambia el idioma de la app al momento. */
class SetAppLanguageUseCase @Inject constructor(
    private val repository: AppLanguageRepository
) {
    operator fun invoke(language: AppLanguage) = repository.set(language)
}
