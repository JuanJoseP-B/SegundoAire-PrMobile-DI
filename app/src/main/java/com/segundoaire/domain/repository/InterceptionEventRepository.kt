package com.segundoaire.domain.repository

import com.segundoaire.domain.model.InterceptionEvent
import com.segundoaire.domain.model.InterceptionResult
import kotlinx.coroutines.flow.Flow

interface InterceptionEventRepository {
    /** Guarda una intercepción todavía sin resultado y devuelve su id. */
    suspend fun record(packageName: String, timestampMillis: Long): Long

    suspend fun setResult(id: Long, result: InterceptionResult)

    /**
     * Intercepciones desde [fromMillis] (incluido), de la más antigua a la más reciente.
     * Con el modo demo activo son las de ejemplo; guardar sigue yendo al historial real.
     */
    fun observeSince(fromMillis: Long): Flow<List<InterceptionEvent>>
}
