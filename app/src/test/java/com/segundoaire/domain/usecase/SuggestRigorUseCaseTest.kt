package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.RiskMoment
import com.segundoaire.domain.model.UsageRange
import org.junit.Assert.assertEquals
import org.junit.Test

class SuggestRigorUseCaseTest {

    private val suggestRigor = SuggestRigorUseCase()

    @Test
    fun `uso moderado sin riesgo de concentracion sugiere Mindful`() = assertEquals(
        RigorLevel.MINDFUL,
        suggestRigor(UsageRange.FROM_4_TO_6, listOf(Goal.SLEEP), setOf(RiskMoment.BEFORE_SLEEP))
    )

    @Test
    fun `uso de 6 a 8 horas sugiere Strict`() = assertEquals(
        RigorLevel.STRICT,
        suggestRigor(UsageRange.FROM_6_TO_8, listOf(Goal.SLEEP), setOf(RiskMoment.BORED))
    )

    @Test
    fun `uso de mas de 8 horas sugiere Strict`() = assertEquals(
        RigorLevel.STRICT,
        suggestRigor(UsageRange.OVER_8, emptyList(), emptySet())
    )

    @Test
    fun `querer concentrarse y usarlo al estudiar sugiere Strict`() = assertEquals(
        RigorLevel.STRICT,
        suggestRigor(UsageRange.UNDER_2, listOf(Goal.FOCUS), setOf(RiskMoment.STUDY_OR_WORK))
    )

    @Test
    fun `querer concentrarse sin usarlo al estudiar sugiere Mindful`() = assertEquals(
        RigorLevel.MINDFUL,
        suggestRigor(UsageRange.FROM_2_TO_4, listOf(Goal.FOCUS), setOf(RiskMoment.BORED))
    )

    @Test
    fun `sin datos sugiere Mindful`() = assertEquals(
        RigorLevel.MINDFUL,
        suggestRigor(null, emptyList(), emptySet())
    )
}
