package com.segundoaire.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.segundoaire.data.local.dao.AppUsageDao
import com.segundoaire.data.local.dao.BlockSessionDao
import com.segundoaire.data.local.dao.BlockedAppDao
import com.segundoaire.data.local.dao.InterceptionEventDao
import com.segundoaire.data.local.entity.AppUsageEntity
import com.segundoaire.data.local.entity.BlockSessionEntity
import com.segundoaire.data.local.entity.BlockedAppEntity
import com.segundoaire.data.local.entity.InterceptionEventEntity

@Database(
    entities = [
        BlockedAppEntity::class,
        InterceptionEventEntity::class,
        BlockSessionEntity::class,
        AppUsageEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun interceptionEventDao(): InterceptionEventDao
    abstract fun blockSessionDao(): BlockSessionDao
    abstract fun appUsageDao(): AppUsageDao

    companion object {
        const val NAME = "segundoaire.db"
    }
}
