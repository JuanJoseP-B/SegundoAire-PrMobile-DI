package com.segundoaire.domain.repository

import com.segundoaire.domain.model.BlockSession
import kotlinx.coroutines.flow.Flow

interface BlockSessionRepository {
    /** Guarda un bloque terminado. Idempotente: el mismo `startedAt` no se duplica. */
    suspend fun record(session: BlockSession)

    /**
     * Bloques completados que terminaron desde [fromMillis] (incluido), por fecha de fin.
     * Con el modo demo activo son los de ejemplo; guardar sigue yendo al historial real.
     */
    fun observeCompletedSince(fromMillis: Long): Flow<List<BlockSession>>
}
