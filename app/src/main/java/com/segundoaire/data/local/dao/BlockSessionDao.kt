package com.segundoaire.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.segundoaire.data.local.entity.BlockSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockSessionDao {

    // started_at es único: un bloque que se registra dos veces se queda con la primera
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: BlockSessionEntity)

    @Query("SELECT * FROM block_sessions WHERE completed = 1 AND ended_at >= :fromMillis ORDER BY ended_at")
    fun observeCompletedSince(fromMillis: Long): Flow<List<BlockSessionEntity>>
}
