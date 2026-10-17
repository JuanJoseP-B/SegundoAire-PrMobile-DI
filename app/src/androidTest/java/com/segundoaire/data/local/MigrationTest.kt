package com.segundoaire.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * La migración 1→2 contra los esquemas exportados en `app/schemas`. Usa su propia
 * base de prueba: no toca la de la app.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @After
    fun tearDown() {
        context.deleteDatabase(TEST_DB)
    }

    @Test
    fun migrar_de_1_a_2_conserva_las_apps_y_crea_el_historial() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.insert(
                "blocked_apps",
                SQLiteDatabase.CONFLICT_NONE,
                ContentValues().apply {
                    put("package_name", "com.instagram.android")
                    put("app_name", "Instagram")
                    put("cooldown_minutes", 5)
                    put("is_blocked", 1)
                }
            )
        }

        // Valida que el resultado coincide exactamente con schemas/2.json
        helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2).use { db ->
            db.query("SELECT app_name, cooldown_minutes, is_blocked FROM blocked_apps").use { cursor ->
                assertEquals(1, cursor.count)
                cursor.moveToFirst()
                assertEquals("Instagram", cursor.getString(0))
                assertEquals(5, cursor.getInt(1))
                assertEquals(1, cursor.getInt(2))
            }

            db.execSQL(
                "INSERT INTO interception_events (package_name, timestamp_millis, result) " +
                    "VALUES ('com.instagram.android', 1000, NULL)"
            )
            db.execSQL("UPDATE interception_events SET result = 'VOLVIO' WHERE id = 1")
            db.query("SELECT result FROM interception_events").use { cursor ->
                cursor.moveToFirst()
                assertEquals("VOLVIO", cursor.getString(0))
            }

            // started_at es único: registrar dos veces el mismo bloque deja una sola fila
            repeat(2) {
                db.execSQL(
                    "INSERT OR IGNORE INTO block_sessions " +
                        "(started_at, ended_at, duration_minutes, rigor, completed) " +
                        "VALUES (1000, 901000, 15, 'STRICT', 1)"
                )
            }
            db.query("SELECT COUNT(*) FROM block_sessions").use { cursor ->
                cursor.moveToFirst()
                assertEquals(1, cursor.getInt(0))
            }
        }
    }

    @Test
    fun room_abre_la_base_migrada_sin_rehacerla() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.execSQL(
                "INSERT INTO blocked_apps (package_name, app_name, cooldown_minutes, is_blocked) " +
                    "VALUES ('com.zhiliaoapp.musically', 'TikTok', 5, 1)"
            )
        }

        // Sin fallbackToDestructiveMigration: si la migración no cuadra, Room falla aquí
        val database = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB)
            .addMigrations(MIGRATION_1_2)
            .build()
        try {
            database.openHelper.writableDatabase.query("SELECT COUNT(*) FROM blocked_apps").use { cursor ->
                cursor.moveToFirst()
                assertEquals(1, cursor.getInt(0))
            }
            assertEquals(2, database.openHelper.readableDatabase.version)
        } finally {
            database.close()
        }
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
    }
}
