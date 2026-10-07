package com.segundoaire.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.segundoaire.data.local.entity.BlockedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAppDao {

    @Query("SELECT * FROM blocked_apps ORDER BY app_name COLLATE NOCASE")
    fun observeAll(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE package_name = :packageName LIMIT 1")
    suspend fun getByPackage(packageName: String): BlockedAppEntity?

    @Upsert
    suspend fun upsert(entity: BlockedAppEntity)

    @Query("UPDATE blocked_apps SET is_blocked = :isBlocked WHERE package_name = :packageName")
    suspend fun setBlocked(packageName: String, isBlocked: Boolean)

    @Query("DELETE FROM blocked_apps WHERE package_name = :packageName")
    suspend fun delete(packageName: String)
}
