package com.segundoaire.presentation.onboarding

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/** Resultado de tocar una meta: se agrega, se quita o no cabe una tercera. */
internal sealed interface PickResult {
    data class Added(val picks: List<Int>) : PickResult
    data class Removed(val picks: List<Int>) : PickResult
    data object Denied : PickResult
}

/**
 * Reglas del flujo que no dibujan nada: cuentas del hold, la fórmula de la revelación, los
 * retrasos de las cascadas y el límite de metas. Aparte de la UI para poder probarlas.
 */
internal object OnboardingRules {
    const val MaxGoals = 2
    const val NameMaxLength = 20

    // Mantener presionado: llena en 1,5 s y, al soltar, se vacía en 300 ms
    const val HoldFillMillis = 1500f
    const val HoldReleaseMillis = 300f
    const val FirstMark = 0.33f
    const val SecondMark = 0.66f
    const val CommitAdvanceMillis = 1600L

    // Respiración: tres ciclos de 8 s y "Saltar" pasa a "Continuar" a los 24 s
    const val BreathCycles = 3
    const val BreathDoneMillis = 24_000L

    // Pausa de la pantalla de entrada: dura 1,5 s antes de pasar al cierre
    const val LoginBusyMillis = 1500L
    const val LoginTimeoutMillis = 6000L

    // Horas al día que representa cada rango de uso (el último es "más de 8 h")
    private val UsageHours = doubleArrayOf(1.5, 3.0, 5.0, 7.0, 9.0)
    const val ProjectionYears = 50
    private const val DaysPerYear = 365
    private const val HoursPerYear = 8760

    fun usageHours(index: Int): Double = UsageHours[index.coerceIn(0, UsageHours.lastIndex)]

    /** Años frente a una pantalla en 50 años: `h × 365 × 50 ÷ 8.760`. */
    fun projectedYears(hours: Double): Double = hours * DaysPerYear * ProjectionYears / HoursPerYear

    /** Años que se ganan al recuperar 1 h al día. */
    fun recoveredYears(): Double = projectedYears(1.0)

    fun formatYears(value: Double, locale: Locale): String = decimalFormat(locale).format(value)

    /** `5 h × 365 × 50 ÷ 8.760`, con los separadores del idioma. */
    fun formula(hours: Double, locale: Locale): String {
        val hoursText = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 1
        }.format(hours)
        val yearHours = NumberFormat.getIntegerInstance(locale).format(HoursPerYear)
        return "$hoursText h × $DaysPerYear × $ProjectionYears ÷ $yearHours"
    }

    fun decimalFormat(locale: Locale): NumberFormat = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }

    /** Las opciones de Objetivo y Momento entran en cascada: 80 ms más 60 ms por posición. */
    fun cascadeDelayMillis(index: Int): Int = 80 + 60 * index.coerceAtLeast(0)

    /** Las filas del plan entran cada 90 ms, desde los 200 ms. */
    fun planRowDelayMillis(index: Int): Int = 200 + 90 * index.coerceAtLeast(0)

    /** Nuevo progreso del hold tras [dtMillis]: sube con el dedo puesto y baja al soltar. */
    fun advanceHold(hold: Float, holding: Boolean, dtMillis: Float): Float =
        if (holding) min(1f, hold + dtMillis / HoldFillMillis) else max(0f, hold - dtMillis / HoldReleaseMillis)

    /** Marcas del 33 % y del 66 % ya cruzadas (0, 1 o 2). */
    fun marksReached(hold: Float): Int = (if (hold >= FirstMark) 1 else 0) + (if (hold >= SecondMark) 1 else 0)

    /** Tocar la meta [index]: quita la que ya estaba, agrega si cabe y rechaza la tercera. */
    fun togglePick(picks: List<Int>, index: Int): PickResult = when {
        index in picks -> PickResult.Removed(picks - index)
        picks.size >= MaxGoals -> PickResult.Denied
        else -> PickResult.Added(picks + index)
    }

    /** Hojas del árbol por letra del nombre, hasta 10. */
    fun nameLeaves(name: String): Int = min(name.length, 10)

    /** El saludo usa el nombre escrito, o "Juan" mientras el campo está vacío en el prototipo. */
    fun displayName(name: String, fallback: String): String = name.trim().ifEmpty { fallback }

    /**
     * Parte un texto con `*palabra*` en tramos normales y de énfasis. Un asterisco sin pareja
     * se queda como texto.
     */
    fun splitEmphasis(text: String): List<Pair<String, Boolean>> {
        val parts = ArrayList<Pair<String, Boolean>>()
        var index = 0
        while (index < text.length) {
            val open = text.indexOf('*', index)
            val close = if (open >= 0) text.indexOf('*', open + 1) else -1
            if (open < 0 || close < 0) {
                parts += text.substring(index) to false
                break
            }
            if (open > index) parts += text.substring(index, open) to false
            parts += text.substring(open + 1, close) to true
            index = close + 1
        }
        return parts
    }
}
