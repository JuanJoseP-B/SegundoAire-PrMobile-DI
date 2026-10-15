package com.segundoaire.domain.repository

import java.io.IOException

/**
 * Borra todo lo que la app guarda en el dispositivo: preferencias, perfil, apps
 * vigiladas y el historial. No toca lo que no es de la app, como los permisos
 * del sistema.
 */
interface AppResetRepository {
    /** @throws IOException si no se pudo borrar; en ese caso el perfil sigue en pie. */
    suspend fun clearAll()
}
