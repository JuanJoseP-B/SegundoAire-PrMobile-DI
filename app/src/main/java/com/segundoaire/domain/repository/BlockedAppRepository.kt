package com.segundoaire.domain.repository

import com.segundoaire.domain.model.BlockedApp
import kotlinx.coroutines.flow.Flow

interface BlockedAppRepository {
    fun observeAll(): Flow<List<BlockedApp>>
    suspend fun getByPackage(packageName: String): BlockedApp?
    suspend fun upsert(app: BlockedApp)
    suspend fun setBlocked(packageName: String, isBlocked: Boolean)
    suspend fun delete(packageName: String)

    /**
     * Deja exactamente [apps] en la lista: guarda las nuevas o cambiadas y borra
     * el resto, todo en una transacción para no dejar una selección a medias.
     */
    suspend fun replaceAll(apps: List<BlockedApp>)
}
