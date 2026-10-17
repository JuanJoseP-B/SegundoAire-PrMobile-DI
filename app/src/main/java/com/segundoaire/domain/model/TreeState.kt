package com.segundoaire.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * El árbol del usuario fuera del onboarding: lo comparten el Panel, el Perfil, el
 * GlassWall y la imagen para compartir.
 *
 * - Las flores salen de las [goals] del onboarding, una por meta.
 * - Cada pausa respetada de la semana ([weekLeaves]) agrega una hoja, hasta [MAX_LEAVES].
 * - [seenLeaves] son las que ya brotaron en pantalla: las demás brotan al volver al Panel.
 * - [plantedAt] es el día en que se completó el onboarding.
 */
data class TreeState(
    val goals: List<Goal> = emptyList(),
    val weekLeaves: Int = 0,
    val seenLeaves: Int = 0,
    val plantedAt: LocalDate? = null
) {
    /** Hojas dibujadas en la copa. */
    val leafCount: Int get() = weekLeaves.coerceIn(0, MAX_LEAVES)

    /** Hojas que ya estaban: se dibujan sin animar. */
    val shownLeaves: Int get() = seenLeaves.coerceIn(0, leafCount)

    /** Hojas que todavía no brotaron en pantalla. */
    val newLeaves: Int get() = leafCount - shownLeaves

    companion object {
        /** Huecos para hojas en la copa (`leafSlots` de la referencia). */
        const val MAX_LEAVES = 8

        /**
         * Las hojas vistas se guardan junto con la semana en que se vieron: al empezar
         * otra semana la copa se vacía y nada de lo visto antes cuenta.
         */
        fun seenFor(storedWeekStart: LocalDate?, storedSeen: Int, currentWeekStart: LocalDate): Int =
            if (storedWeekStart == currentWeekStart) storedSeen.coerceAtLeast(0) else 0
    }
}

/** Lunes de la semana de [this]: las semanas de la app van de lunes a domingo. */
fun LocalDate.weekStart(): LocalDate = with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
