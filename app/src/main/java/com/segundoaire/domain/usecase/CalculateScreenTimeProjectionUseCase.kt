package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.UsageRange
import javax.inject.Inject

/**
 * Resultado de la revelación del onboarding. Lleva todos los términos de la
 * fórmula para que la pantalla pueda mostrarla tal cual se calculó.
 */
data class ScreenTimeProjection(
    val hoursPerDay: Int,
    val horizonYears: Int,
    /** Años completos frente a la pantalla, en décimas (83 = 8,3 años). */
    val screenYearsTenths: Int,
    val recoverablePercent: Int,
    /** Días completos (de 24 h) que se recuperan en un año. */
    val recoverableDaysPerYear: Int
)

/**
 * Proyección honesta del tiempo de pantalla, sin supuestos ocultos:
 *
 * - años de pantalla = horas al día × [HORIZON_YEARS] años ÷ 24 h del día
 * - días recuperables al año = [RECOVERABLE_PERCENT] % × horas al día × 365 ÷ 24
 *
 * Usa el día completo de 24 h (no solo las horas despierto) para no inflar la
 * cifra, y redondea al valor más cercano.
 */
class CalculateScreenTimeProjectionUseCase @Inject constructor() {
    operator fun invoke(usageRange: UsageRange): ScreenTimeProjection {
        val hours = usageRange.typicalHoursPerDay
        return ScreenTimeProjection(
            hoursPerDay = hours,
            horizonYears = HORIZON_YEARS,
            screenYearsTenths = divideRounded(hours * HORIZON_YEARS * 10, HOURS_PER_DAY),
            recoverablePercent = RECOVERABLE_PERCENT,
            recoverableDaysPerYear = divideRounded(
                hours * DAYS_PER_YEAR * RECOVERABLE_PERCENT,
                HOURS_PER_DAY * 100
            )
        )
    }

    private fun divideRounded(dividend: Int, divisor: Int): Int = (dividend + divisor / 2) / divisor

    companion object {
        const val HORIZON_YEARS = 40
        const val RECOVERABLE_PERCENT = 25
        const val HOURS_PER_DAY = 24
        const val DAYS_PER_YEAR = 365
    }
}
