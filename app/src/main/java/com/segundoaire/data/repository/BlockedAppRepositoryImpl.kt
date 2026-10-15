package com.segundoaire.data.repository

import androidx.room.withTransaction
import com.segundoaire.data.local.AppDatabase
import com.segundoaire.data.local.dao.BlockedAppDao
import com.segundoaire.data.local.mapper.BlockedAppMapper
import com.segundoaire.domain.model.BlockedApp
import com.segundoaire.domain.repository.BlockedAppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Room es la única fuente de verdad (SSOT); no hay caché propia en este nivel. */
@Singleton
class BlockedAppRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val dao: BlockedAppDao
) : BlockedAppRepository {

    override fun observeAll(): Flow<List<BlockedApp>> =
        dao.observeAll().map { list -> list.map(BlockedAppMapper::toDomain) }

    override suspend fun getByPackage(packageName: String): BlockedApp? =
        dao.getByPackage(packageName)?.let(BlockedAppMapper::toDomain)

    override suspend fun upsert(app: BlockedApp) = dao.upsert(BlockedAppMapper.toEntity(app))

    override suspend fun setBlocked(packageName: String, isBlocked: Boolean) =
        dao.setBlocked(packageName, isBlocked)

    override suspend fun delete(packageName: String) = dao.delete(packageName)

    override suspend fun replaceAll(apps: List<BlockedApp>) {
        database.withTransaction {
            dao.deleteNotIn(apps.map { it.packageName })
            dao.upsertAll(apps.map(BlockedAppMapper::toEntity))
        }
    }
}
