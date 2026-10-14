package com.segundoaire.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.segundoaire.data.local.dao.BlockedAppDao
import com.segundoaire.data.local.entity.BlockSessionEntity
import com.segundoaire.data.local.entity.BlockedAppEntity
import com.segundoaire.data.local.entity.InterceptionEventEntity

@Database(
    entities = [
        BlockedAppEntity::class,
        InterceptionEventEntity::class,
        BlockSessionEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedAppDao(): BlockedAppDao

    companion object {
        const val NAME = "segundoaire.db"
    }
}
