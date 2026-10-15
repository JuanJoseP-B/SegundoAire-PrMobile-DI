package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.AppLanguage
import com.segundoaire.domain.repository.AppLanguageRepository
import javax.inject.Inject

/** Idioma elegido dentro de la app, leído en el momento de pedirlo. */
class GetAppLanguageUseCase @Inject constructor(
    private val repository: AppLanguageRepository
) {
    operator fun invoke(): AppLanguage = repository.current()
}
