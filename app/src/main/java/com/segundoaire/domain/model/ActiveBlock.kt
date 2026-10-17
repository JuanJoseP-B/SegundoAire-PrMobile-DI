package com.segundoaire.domain.model

/**
 * Bloque de enfoque en curso. [startedAt] y [endsAt] son instantes del reloj de
 * pared (ms desde epoch): el tiempo restante siempre se deriva de [endsAt] y la
 * hora actual, así sobrevive a la rotación, al segundo plano y al cierre de la app.
 */
data class ActiveBlock(
    val startedAt: Long,
    val endsAt: Long,
    val rigor: RigorLevel,
    val durationMinutes: Int
) {
    val totalMillis: Long get() = endsAt - startedAt

    /** Lo que falta en [now]; nunca negativo ni mayor que la duración del bloque. */
    fun remainingMillis(now: Long): Long = (endsAt - now).coerceIn(0L, totalMillis)

    fun isActiveAt(now: Long): Boolean = now < endsAt

    /** Avance de 0 a 1 en [now]. */
    fun progress(now: Long): Float =
        if (totalMillis <= 0L) 1f else 1f - remainingMillis(now).toFloat() / totalMillis

    /** Segundos que se muestran: hacia arriba, para no enseñar 0:00 con el bloque vivo. */
    fun remainingSeconds(now: Long): Int = ((remainingMillis(now) + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND).toInt()

    companion object {
        const val MILLIS_PER_MINUTE = 60_000L
        const val MILLIS_PER_SECOND = 1_000L
        const val DEFAULT_DURATION_MINUTES = 15
        val DURATION_OPTIONS_MINUTES = listOf(5, 10, 15, 30)

        fun starting(now: Long, durationMinutes: Int, rigor: RigorLevel) = ActiveBlock(
            startedAt = now,
            endsAt = now + durationMinutes * MILLIS_PER_MINUTE,
            rigor = rigor,
            durationMinutes = durationMinutes
        )
    }
}
