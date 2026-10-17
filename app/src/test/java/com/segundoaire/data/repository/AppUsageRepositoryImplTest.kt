package com.segundoaire.data.repository

import com.segundoaire.data.local.entity.AppUsageEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class AppUsageRepositoryImplTest {

    @Test
    fun suma_por_app_y_recorta_lo_que_empezo_antes_de_la_ventana() {
        val rows = listOf(
            AppUsageEntity(packageName = "video", startedAt = 1_000, endedAt = 61_000),
            AppUsageEntity(packageName = "video", startedAt = 100_000, endedAt = 130_000),
            // Empezó antes de la ventana: solo cuenta desde 50 000
            AppUsageEntity(packageName = "social", startedAt = 20_000, endedAt = 80_000)
        )

        val totals = AppUsageRepositoryImpl.totalsSince(rows, fromMillis = 50_000)

        assertEquals(mapOf("video" to 41_000L, "social" to 30_000L), totals)
    }
}
