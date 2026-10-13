package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.RiskMoment
import com.segundoaire.domain.model.UsageRange
import javax.inject.Inject

/**
 * Rigor sugerido en el plan. Strict solo cuando el uso es alto (6 h o más al
 * día) o cuando el usuario quiere concentrarse y su momento de mayor uso es
 * justo mientras estudia o trabaja; en cualquier otro caso, Mindful.
 */
class SuggestRigorUseCase @Inject constructor() {
    operator fun invoke(
        usageRange: UsageRange?,
        goals: Collection<Goal>,
        riskMoments: Collection<RiskMoment>
    ): RigorLevel {
        val heavyUse = usageRange != null && usageRange >= UsageRange.FROM_6_TO_8
        val focusAtRisk = Goal.FOCUS in goals && RiskMoment.STUDY_OR_WORK in riskMoments
        return if (heavyUse || focusAtRisk) RigorLevel.STRICT else RigorLevel.MINDFUL
    }
}
