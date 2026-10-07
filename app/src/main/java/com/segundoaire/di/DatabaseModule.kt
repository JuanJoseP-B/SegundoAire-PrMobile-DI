package com.segundoaire.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.segundoaire.data.local.AppDatabase
import com.segundoaire.data.local.dao.BlockedAppDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .addCallback(SeedCallback)
            .build()

    @Provides
    fun provideBlockedAppDao(db: AppDatabase): BlockedAppDao = db.blockedAppDao()

    /** Reglas iniciales (las mismas que antes eran una lista estática en el motor). */
    private object SeedCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            listOf(
                "com.instagram.android" to "Instagram",
                "com.zhiliaoapp.musically" to "TikTok",
                "com.facebook.katana" to "Facebook",
                "com.twitter.android" to "X"
            ).forEach { (pkg, name) ->
                db.execSQL(
                    "INSERT OR IGNORE INTO blocked_apps " +
                        "(package_name, app_name, cooldown_minutes, is_blocked) VALUES (?, ?, 5, 1)",
                    arrayOf<Any>(pkg, name)
                )
            }
        }
    }
}
