package com.segundoaire.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Una aparición del GlassWall. Las estadísticas filtran por fecha, de ahí el índice. */
@Entity(
    tableName = "interception_events",
    indices = [Index(value = ["timestamp_millis"])]
)
data class InterceptionEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "package_name") val packageName: String,
    @ColumnInfo(name = "timestamp_millis") val timestampMillis: Long,
    // Nombre del enum InterceptionResult; null mientras el GlassWall sigue a la vista
    @ColumnInfo(name = "result") val result: String?,
    // Nombre del enum InterceptionIntent; null si no eligió ningún chip (v3)
    @ColumnInfo(name = "intent") val intent: String? = null
)
