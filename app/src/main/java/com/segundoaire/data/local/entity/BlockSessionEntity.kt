package com.segundoaire.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Un bloque de enfoque que ya terminó, completado o detenido antes de tiempo.
 * [startedAt] es único: registrar dos veces el mismo bloque no lo duplica.
 */
@Entity(
    tableName = "block_sessions",
    indices = [
        Index(value = ["started_at"], unique = true),
        Index(value = ["ended_at"])
    ]
)
data class BlockSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "ended_at") val endedAt: Long,
    @ColumnInfo(name = "duration_minutes") val durationMinutes: Int,
    // Nombre del enum RigorLevel
    @ColumnInfo(name = "rigor") val rigor: String,
    @ColumnInfo(name = "completed") val completed: Boolean
)
