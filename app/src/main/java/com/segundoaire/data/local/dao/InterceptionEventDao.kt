package com.segundoaire.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.segundoaire.data.local.entity.InterceptionEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InterceptionEventDao {

    @Insert
    suspend fun insert(entity: InterceptionEventEntity): Long

    @Query("UPDATE interception_events SET result = :result WHERE id = :id")
    suspend fun setResult(id: Long, result: String)

    @Query("SELECT * FROM interception_events WHERE timestamp_millis >= :fromMillis ORDER BY timestamp_millis")
    fun observeSince(fromMillis: Long): Flow<List<InterceptionEventEntity>>
}
