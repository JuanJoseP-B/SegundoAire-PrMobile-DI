package com.segundoaire.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.segundoaire.data.local.dao.BlockedAppDao
import com.segundoaire.data.local.entity.BlockedAppEntity

@Database(
    entities = [BlockedAppEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedAppDao(): BlockedAppDao

    companion object {
        const val NAME = "segundoaire.db"
    }
}
