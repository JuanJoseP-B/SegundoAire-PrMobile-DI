package com.segundoaire.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Un rato en una app vigilada (v3). Las lecturas piden lo que terminó desde una fecha. */
@Entity(
    tableName = "app_usage_sessions",
    indices = [Index(value = ["ended_at"])]
)
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "package_name") val packageName: String,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "ended_at") val endedAt: Long
)
