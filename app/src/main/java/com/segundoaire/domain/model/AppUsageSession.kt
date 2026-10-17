package com.segundoaire.domain.model

/**
 * Un rato en una app vigilada, medido con los eventos del servicio de accesibilidad.
 * Es una estimación: se cierra al cambiar de app o al apagar la pantalla. Los instantes
 * son ms desde epoch.
 */
data class AppUsageSession(val packageName: String, val startedAt: Long, val endedAt: Long) {
    val durationMillis: Long get() = (endedAt - startedAt).coerceAtLeast(0L)
}
