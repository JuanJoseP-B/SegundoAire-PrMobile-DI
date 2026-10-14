package com.segundoaire.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 → v2: historial para Progreso (intercepciones y bloques terminados).
 * Solo agrega tablas; `blocked_apps` no cambia. El SQL es el de `schemas/2.json`.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `interception_events` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`package_name` TEXT NOT NULL, " +
                "`timestamp_millis` INTEGER NOT NULL, " +
                "`result` TEXT)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_interception_events_timestamp_millis` " +
                "ON `interception_events` (`timestamp_millis`)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `block_sessions` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`started_at` INTEGER NOT NULL, " +
                "`ended_at` INTEGER NOT NULL, " +
                "`duration_minutes` INTEGER NOT NULL, " +
                "`rigor` TEXT NOT NULL, " +
                "`completed` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_block_sessions_started_at` " +
                "ON `block_sessions` (`started_at`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_block_sessions_ended_at` " +
                "ON `block_sessions` (`ended_at`)"
        )
    }
}
