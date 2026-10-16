package com.segundoaire.presentation.onboarding

import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.RiskMoment
import com.segundoaire.domain.model.UsageRange

enum class NameError { INVALID_CHARACTERS }

/**
 * Estado de todo el flujo: lo comparten los pasos porque el plan, el compromiso y el
 * árbol muestran lo que se respondió antes. Lo que solo es animación (la caída de hojas,
 * el progreso del hold, el telón) vive en la pantalla y no aquí.
 */
data class OnboardingUiState(
    val step: Int = OnboardingStep.Seed,
    val name: String = "",
    val nameError: NameError? = null,
    val goals: List<Goal> = emptyList(),
    val usageRange: UsageRange = DEFAULT_USAGE_RANGE,
    val riskMoments: Set<RiskMoment> = emptySet(),
    val suggestedRigor: RigorLevel = RigorLevel.MINDFUL,
    val soundEnabled: Boolean = true,
    val isSaving: Boolean = false
) {
    /** Nombre tal como se guarda y se muestra en el resto del flujo. */
    val displayName: String get() = name.trim()

    val isNameValid: Boolean get() = displayName.isNotEmpty() && nameError == null

    /** `true` si lo respondido alcanza para avanzar desde [step]. */
    fun canContinue(step: Int): Boolean = when (step) {
        OnboardingStep.Name -> isNameValid
        OnboardingStep.Goals -> goals.isNotEmpty()
        OnboardingStep.Moment -> riskMoments.isNotEmpty()
        OnboardingStep.Commit -> isProfileValid && !isSaving
        else -> true
    }

    val isProfileValid: Boolean
        get() = canContinue(OnboardingStep.Name) &&
            canContinue(OnboardingStep.Goals) &&
            canContinue(OnboardingStep.Moment)

    companion object {
        // El selector necesita un tramo elegido: se parte del central
        val DEFAULT_USAGE_RANGE = UsageRange.FROM_4_TO_6
    }
}
