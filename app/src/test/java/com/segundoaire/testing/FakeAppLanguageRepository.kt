package com.segundoaire.testing

import com.segundoaire.domain.model.AppLanguage
import com.segundoaire.domain.repository.AppLanguageRepository

/**
 * Idioma en memoria. [changeExternally] simula un cambio hecho desde los ajustes
 * de idioma por app del sistema, sin pasar por la app.
 */
class FakeAppLanguageRepository(initial: AppLanguage = AppLanguage.SYSTEM) : AppLanguageRepository {
    private var language = initial

    /** Veces que la app pidió cambiar el idioma. */
    var setCount = 0
        private set

    override fun current(): AppLanguage = language

    override fun set(language: AppLanguage) {
        this.language = language
        setCount++
    }

    fun changeExternally(language: AppLanguage) {
        this.language = language
    }
}
