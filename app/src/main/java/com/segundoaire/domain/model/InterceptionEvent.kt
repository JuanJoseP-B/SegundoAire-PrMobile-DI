package com.segundoaire.domain.model

/** Cómo se cerró el GlassWall: el usuario volvió o siguió hacia la app. */
enum class InterceptionResult { VOLVIO, CONTINUO }

/**
 * Una aparición del GlassWall sobre [packageName] en [timestampMillis] (ms desde epoch).
 * [result] es `null` mientras sigue a la vista o si el servicio se fue antes de cerrarlo.
 */
data class InterceptionEvent(
    val id: Long,
    val packageName: String,
    val timestampMillis: Long,
    val result: InterceptionResult?
)
