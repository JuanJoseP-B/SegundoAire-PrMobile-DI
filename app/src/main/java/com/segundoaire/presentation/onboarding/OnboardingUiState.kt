package com.segundoaire.presentation.onboarding

import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.RiskMoment
import com.segundoaire.domain.model.UsageRange
import com.segundoaire.domain.usecase.ScreenTimeProjection

enum class NameError { INVALID_CHARACTERS }

/**
 * Estado de todo el flujo: lo comparten los pasos porque el plan y el
 * compromiso muestran lo que se respondió antes.
 */
data class OnboardingUiState(
    val welcomePage: Int = 0,
    val name: String = "",
    val nameError: NameError? = null,
    val goals: List<Goal> = emptyList(),
    val usageRange: UsageRange = DEFAULT_USAGE_RANGE,
    val riskMoments: Set<RiskMoment> = emptySet(),
    val suggestedRigor: RigorLevel = RigorLevel.MINDFUL,
    val projection: ScreenTimeProjection,
    val breathCyclesDone: Int = 0,
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false
) {
    val isLastWelcomePage: Boolean get() = welcomePage == WELCOME_PAGES - 1

    val isBreathingDone: Boolean get() = breathCyclesDone >= BREATH_CYCLES

    /** Nombre tal como se guarda y se muestra en el resto del flujo. */
    val displayName: String get() = name.trim()

    /** `true` si lo respondido en [step] alcanza para avanzar. */
    fun canContinue(step: OnboardingStep): Boolean = when (step) {
        OnboardingStep.NAME -> displayName.isNotEmpty() && nameError == null
        OnboardingStep.GOALS -> goals.isNotEmpty()
        OnboardingStep.MOMENTS -> riskMoments.isNotEmpty()
        OnboardingStep.COMMITMENT -> isProfileValid && !isSaving
        else -> true
    }

    val isProfileValid: Boolean
        get() = canContinue(OnboardingStep.NAME) &&
            canContinue(OnboardingStep.GOALS) &&
            canContinue(OnboardingStep.MOMENTS)

    companion object {
        const val WELCOME_PAGES = 3
        const val BREATH_CYCLES = 3

        // El selector necesita un tramo elegido: se parte del central
        val DEFAULT_USAGE_RANGE = UsageRange.FROM_4_TO_6
    }
}
