package com.segundoaire.domain.repository

import com.segundoaire.domain.model.AppUsageSession
import kotlinx.coroutines.flow.Flow

interface AppUsageRepository {
    /** Guarda un lote de ratos ya cerrados. */
    suspend fun record(sessions: List<AppUsageSession>)

    /**
     * Milisegundos de uso por paquete desde [fromMillis]; lo que empezó antes se recorta a
     * la ventana. Con el modo demo activo son cifras de ejemplo para las apps vigiladas.
     */
    fun observeUsageSince(fromMillis: Long): Flow<Map<String, Long>>
}
