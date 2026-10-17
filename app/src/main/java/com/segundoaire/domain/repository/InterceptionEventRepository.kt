package com.segundoaire.domain.repository

import com.segundoaire.domain.model.InterceptionEvent
import com.segundoaire.domain.model.InterceptionIntent
import com.segundoaire.domain.model.InterceptionResult
import kotlinx.coroutines.flow.Flow

interface InterceptionEventRepository {
    /** Guarda una intercepción todavía sin resultado y devuelve su id. */
    suspend fun record(packageName: String, timestampMillis: Long): Long

    suspend fun setResult(id: Long, result: InterceptionResult)

    /** Guarda para qué entraba el usuario (chips de Mindful). */
    suspend fun setIntent(id: Long, intent: InterceptionIntent)

    /**
     * Intercepciones desde [fromMillis] (incluido), de la más antigua a la más reciente.
     * Con el modo demo activo son las de ejemplo; guardar sigue yendo al historial real.
     */
    fun observeSince(fromMillis: Long): Flow<List<InterceptionEvent>>
}
