package com.segundoaire.presentation.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.RiskMoment
import com.segundoaire.domain.model.UsageRange
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.domain.usecase.CompleteOnboardingUseCase
import com.segundoaire.domain.usecase.SaveUserProfileUseCase
import com.segundoaire.domain.usecase.SuggestRigorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.IOException
import javax.inject.Inject

/**
 * ViewModel del flujo completo. Guarda el paso y las respuestas en el [SavedStateHandle]
 * para que, si el sistema mata el proceso a mitad del flujo, el paso restaurado no aparezca
 * vacío. El perfil se escribe en DataStore al completar el compromiso ([commit]).
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val suggestRigor: SuggestRigorUseCase,
    private val saveUserProfile: SaveUserProfileUseCase,
    private val completeOnboarding: CompleteOnboardingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(restoreState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onStepChange(step: Int) {
        val next = step.coerceIn(OnboardingStep.Seed, OnboardingStep.Last)
        _uiState.update { it.copy(step = next) }
        savedStateHandle[KEY_STEP] = next
    }

    /** Entra por un paso más avanzado (la cuenta pendiente) sin retroceder si ya se pasó de él. */
    internal fun ensureStartStep(step: Int) {
        if (_uiState.value.step < step) onStepChange(step)
    }

    fun onNameChange(value: String) {
        val name = value.take(UserProfile.MAX_NAME_LENGTH)
        updateAnswers {
            it.copy(
                name = name,
                nameError = if (UserProfile.isValidName(name)) null else NameError.INVALID_CHARACTERS
            )
        }
    }

    /** [goals] en el orden en que se eligieron; lo que pase del límite se descarta. */
    fun onGoalsChange(goals: List<Goal>) {
        updateAnswers { it.copy(goals = goals.distinct().take(UserProfile.MAX_GOALS)) }
    }

    fun onUsageRangeChange(usageRange: UsageRange) {
        updateAnswers { it.copy(usageRange = usageRange) }
    }

    fun onRiskMomentsChange(riskMoments: Set<RiskMoment>) {
        updateAnswers { it.copy(riskMoments = riskMoments) }
    }

    fun onSoundToggle() {
        _uiState.update { it.copy(soundEnabled = !it.soundEnabled) }
        savedStateHandle[KEY_SOUND] = _uiState.value.soundEnabled
    }

    /**
     * Guarda el perfil y marca el onboarding como completo. Devuelve `false` si falta una
     * respuesta o DataStore no pudo escribir: el compromiso vuelve a quedar disponible.
     * Guarda siempre: si el usuario volvió y cambió algo, el perfil debe quedar al día.
     */
    suspend fun commit(): Boolean {
        val state = _uiState.value
        if (state.isSaving || !state.isProfileValid) return false

        _uiState.update { it.copy(isSaving = true) }
        val saved = try {
            saveUserProfile(
                UserProfile(
                    name = state.displayName,
                    goals = state.goals,
                    usageRange = state.usageRange,
                    riskMoments = state.riskMoments,
                    suggestedRigor = state.suggestedRigor
                )
            )
            completeOnboarding()
            true
        } catch (_: IOException) {
            false
        }
        _uiState.update { it.copy(isSaving = false) }
        return saved
    }

    // Toda respuesta nueva recalcula lo que depende de ella y se copia al estado guardado
    private fun updateAnswers(transform: (OnboardingUiState) -> OnboardingUiState) {
        _uiState.update { withDerived(transform(it)) }
        val state = _uiState.value
        savedStateHandle[KEY_NAME] = state.name
        savedStateHandle[KEY_GOALS] = ArrayList(state.goals.map { it.name })
        savedStateHandle[KEY_USAGE_RANGE] = state.usageRange.name
        savedStateHandle[KEY_RISK_MOMENTS] = ArrayList(state.riskMoments.map { it.name })
    }

    private fun withDerived(state: OnboardingUiState): OnboardingUiState = state.copy(
        suggestedRigor = suggestRigor(state.usageRange, state.goals, state.riskMoments)
    )

    private fun restoreState(): OnboardingUiState {
        val name = savedStateHandle.get<String>(KEY_NAME).orEmpty()
        val usageRange = savedStateHandle.get<String>(KEY_USAGE_RANGE)
            ?.let { saved -> UsageRange.entries.firstOrNull { it.name == saved } }
            ?: OnboardingUiState.DEFAULT_USAGE_RANGE
        return withDerived(
            OnboardingUiState(
                step = (savedStateHandle.get<Int>(KEY_STEP) ?: OnboardingStep.Seed)
                    .coerceIn(OnboardingStep.Seed, OnboardingStep.Last),
                name = name,
                nameError = if (UserProfile.isValidName(name)) null else NameError.INVALID_CHARACTERS,
                goals = savedStateHandle.get<ArrayList<String>>(KEY_GOALS).orEmpty()
                    .mapNotNull { saved -> Goal.entries.firstOrNull { it.name == saved } },
                usageRange = usageRange,
                riskMoments = savedStateHandle.get<ArrayList<String>>(KEY_RISK_MOMENTS).orEmpty()
                    .mapNotNullTo(linkedSetOf()) { saved -> RiskMoment.entries.firstOrNull { it.name == saved } },
                soundEnabled = savedStateHandle.get<Boolean>(KEY_SOUND) ?: true
            )
        )
    }

    private companion object {
        const val KEY_STEP = "onboarding_step"
        const val KEY_NAME = "onboarding_name"
        const val KEY_GOALS = "onboarding_goals"
        const val KEY_USAGE_RANGE = "onboarding_usage_range"
        const val KEY_RISK_MOMENTS = "onboarding_risk_moments"
        const val KEY_SOUND = "onboarding_sound"
    }
}
