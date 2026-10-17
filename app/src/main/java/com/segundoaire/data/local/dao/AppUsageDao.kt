package com.segundoaire.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.segundoaire.data.local.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {

    @Insert
    suspend fun insertAll(entities: List<AppUsageEntity>)

    @Query("SELECT * FROM app_usage_sessions WHERE ended_at >= :fromMillis")
    fun observeEndedSince(fromMillis: Long): Flow<List<AppUsageEntity>>
}
