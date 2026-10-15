package com.segundoaire.data.local

import android.database.sqlite.SQLiteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

/** Vacía las tablas de Room. Existe para poder probar el reinicio sin una base real. */
interface DatabaseCleaner {
    /** @throws IOException si no se pudo vaciar. */
    suspend fun clearAll()
}

class RoomDatabaseCleaner @Inject constructor(
    private val database: AppDatabase
) : DatabaseCleaner {

    // clearAllTables no puede correr en el hilo principal
    override suspend fun clearAll() {
        try {
            withContext(Dispatchers.IO) { database.clearAllTables() }
        } catch (e: SQLiteException) {
            throw IOException("No se pudo vaciar la base de datos", e)
        }
    }
}
