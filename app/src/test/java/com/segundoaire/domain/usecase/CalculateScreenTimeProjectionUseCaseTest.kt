package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.UsageRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateScreenTimeProjectionUseCaseTest {

    private val calculate = CalculateScreenTimeProjectionUseCase()

    @Test
    fun `expone los terminos de la formula`() {
        val projection = calculate(UsageRange.FROM_4_TO_6)

        assertEquals(5, projection.hoursPerDay)
        assertEquals(40, projection.horizonYears)
        assertEquals(25, projection.recoverablePercent)
    }

    // años = horas × 40 ÷ 24
    @Test
    fun `menos de 2 horas son 1,7 años`() =
        assertEquals(17, calculate(UsageRange.UNDER_2).screenYearsTenths)

    @Test
    fun `de 2 a 4 horas son 5 años exactos`() =
        assertEquals(50, calculate(UsageRange.FROM_2_TO_4).screenYearsTenths)

    @Test
    fun `de 4 a 6 horas son 8,3 años`() =
        assertEquals(83, calculate(UsageRange.FROM_4_TO_6).screenYearsTenths)

    @Test
    fun `de 6 a 8 horas son 11,7 años`() =
        assertEquals(117, calculate(UsageRange.FROM_6_TO_8).screenYearsTenths)

    @Test
    fun `mas de 8 horas usa el minimo del tramo - 13,3 años`() =
        assertEquals(133, calculate(UsageRange.OVER_8).screenYearsTenths)

    // días = 25 % × horas × 365 ÷ 24
    @Test
    fun `dias recuperables al año por tramo`() {
        assertEquals(4, calculate(UsageRange.UNDER_2).recoverableDaysPerYear)
        assertEquals(11, calculate(UsageRange.FROM_2_TO_4).recoverableDaysPerYear)
        assertEquals(19, calculate(UsageRange.FROM_4_TO_6).recoverableDaysPerYear)
        assertEquals(27, calculate(UsageRange.FROM_6_TO_8).recoverableDaysPerYear)
        assertEquals(30, calculate(UsageRange.OVER_8).recoverableDaysPerYear)
    }

    @Test
    fun `mas uso nunca proyecta menos tiempo`() {
        val projections = UsageRange.entries.map(calculate::invoke)

        assertTrue(projections.zipWithNext().all { (less, more) ->
            less.screenYearsTenths <= more.screenYearsTenths &&
                less.recoverableDaysPerYear <= more.recoverableDaysPerYear
        })
    }
}
