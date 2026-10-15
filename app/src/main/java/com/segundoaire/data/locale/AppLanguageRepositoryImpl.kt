package com.segundoaire.data.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.segundoaire.domain.model.AppLanguage
import com.segundoaire.domain.repository.AppLanguageRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLanguageRepositoryImpl @Inject constructor() : AppLanguageRepository {

    override fun current(): AppLanguage {
        val locales = AppCompatDelegate.getApplicationLocales()
        // Sin idioma por app, o con uno que la app no ofrece, vale el del sistema
        return when (if (locales.isEmpty) null else locales[0]?.language) {
            SPANISH_TAG -> AppLanguage.SPANISH
            ENGLISH_TAG -> AppLanguage.ENGLISH
            else -> AppLanguage.SYSTEM
        }
    }

    override fun set(language: AppLanguage) {
        val locales = when (language) {
            AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            AppLanguage.SPANISH -> LocaleListCompat.forLanguageTags(SPANISH_TAG)
            AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags(ENGLISH_TAG)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    private companion object {
        const val SPANISH_TAG = "es"
        const val ENGLISH_TAG = "en"
    }
}
