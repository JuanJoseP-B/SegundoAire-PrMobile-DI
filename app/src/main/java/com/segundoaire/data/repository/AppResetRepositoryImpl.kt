package com.segundoaire.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.segundoaire.data.local.DatabaseCleaner
import com.segundoaire.domain.repository.AppResetRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppResetRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val database: DatabaseCleaner
) : AppResetRepository {

    override suspend fun clearAll() {
        // Room primero: si falla, el perfil sigue en pie y se puede reintentar sin quedar a medias.
        // DataStore va al final porque su vaciado es lo que devuelve la app a la bienvenida.
        database.clearAll()
        dataStore.edit { it.clear() }
    }
}
