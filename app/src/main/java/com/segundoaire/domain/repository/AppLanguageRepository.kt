package com.segundoaire.domain.repository

import com.segundoaire.domain.model.AppLanguage

/**
 * Idioma elegido dentro de la app. No se guarda en DataStore: lo conserva el
 * sistema (Android 13+) o AppCompat (Android 10–12), y es la misma fuente que
 * usan los ajustes de idioma por app del dispositivo, así que se lee en vez de cachearse.
 */
interface AppLanguageRepository {
    fun current(): AppLanguage

    /** Cambia el idioma al momento; la actividad se vuelve a crear con las cadenas nuevas. */
    fun set(language: AppLanguage)
}
