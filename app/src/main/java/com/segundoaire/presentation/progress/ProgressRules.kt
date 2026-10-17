package com.segundoaire.presentation.progress

import kotlin.math.roundToInt

/** Reglas del corte del tronco y de la semana, sin dibujo, aparte para poder probarlas. */
internal object ProgressRules {
    /** Meta diaria: un día con 30 min o más florece y su anillo va en `leaf`. */
    const val GOAL_MINUTES = 30

    /** Anillos que caben en el corte de 260 × 230 (el más grande mide 122 de radio). */
    const val MAX_RINGS = 12

    private const val RingStrokeMin = 1.5f
    private const val MinutesPerStrokeUnit = 12f
    private const val StemMinutesCap = 60
    private const val StemMaxHeight = 96
    private const val StemBase = 6

    fun hasFlower(minutes: Int): Boolean = minutes >= GOAL_MINUTES

    /** Grosor del anillo de un día: un minuto de cada 12, nunca menos de 1,5. */
    fun ringWidth(minutes: Int): Float = maxOf(RingStrokeMin, minutes / MinutesPerStrokeUnit)

    /** Radio horizontal del anillo [index] (0 = el más antiguo, en el centro); el vertical es el 87 %. */
    fun ringRadiusX(index: Int): Float = 14f + (index + 1) * 9f

    /** Leve vaivén del centro de cada anillo (−1, 0 o 1), para que no parezcan dibujados con compás. */
    fun ringWobble(index: Int): Int = index % 3 - 1

    /** Alto del tallo de un día en dp: 96 por hora (hasta una hora) más 6 de base. */
    fun stemHeight(minutes: Int): Int =
        (minutes.coerceIn(0, StemMinutesCap) / 60f * StemMaxHeight).roundToInt() + StemBase

    /** Cuántos anillos dibujar: un día por día plantado, al menos la semana en curso, hasta 12. */
    fun ringCount(daysSincePlanted: Int?, daysThisWeek: Int): Int =
        maxOf(daysThisWeek, (daysSincePlanted ?: MAX_RINGS - 1) + 1).coerceIn(1, MAX_RINGS)
}
