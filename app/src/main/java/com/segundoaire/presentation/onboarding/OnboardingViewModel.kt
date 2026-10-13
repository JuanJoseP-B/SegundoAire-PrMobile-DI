package com.segundoaire.presentation.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.RiskMoment
import com.segundoaire.domain.model.UsageRange
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.domain.usecase.CalculateScreenTimeProjectionUseCase
import com.segundoaire.domain.usecase.CompleteOnboardingUseCase
import com.segundoaire.domain.usecase.SaveUserProfileUseCase
import com.segundoaire.domain.usecase.SuggestRigorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

/**
 * ViewModel compartido por todos los pasos del onboarding (vive en el grafo
 * anidado). Las respuestas se copian al [SavedStateHandle] para que, si el
 * sistema mata el proceso a mitad del flujo, el paso restaurado no aparezca vacío.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val suggestRigor: SuggestRigorUseCase,
    private val calculateProjection: CalculateScreenTimeProjectionUseCase,
    private val saveUserProfile: SaveUserProfileUseCase,
    private val completeOnboarding: CompleteOnboardingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(restoreState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onWelcomeNext() {
        _uiState.update {
            it.copy(welcomePage = (it.welcomePage + 1).coerceAtMost(OnboardingUiState.WELCOME_PAGES - 1))
        }
    }

    fun onWelcomeBack() {
        _uiState.update { it.copy(welcomePage = (it.welcomePage - 1).coerceAtLeast(0)) }
    }

    fun onNameChange(value: String) {
        val name = value.take(UserProfile.MAX_NAME_LENGTH)
        updateAnswers {
            it.copy(
                name = name,
                nameError = if (name.all(::isNameCharacter)) null else NameError.INVALID_CHARACTERS
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

    fun onBreathCycleCompleted() {
        _uiState.update {
            it.copy(breathCyclesDone = (it.breathCyclesDone + 1).coerceAtMost(OnboardingUiState.BREATH_CYCLES))
        }
    }

    /** Guarda el perfil y cierra el onboarding. La pantalla navega al ver `isCompleted`. */
    fun onCommit() {
        val state = _uiState.value
        if (state.isSaving || state.isCompleted || !state.isProfileValid) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
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
                // DataStore no pudo escribir: el botón vuelve a quedar disponible para reintentar
                false
            }
            _uiState.update { it.copy(isSaving = false, isCompleted = saved) }
        }
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
        suggestedRigor = suggestRigor(state.usageRange, state.goals, state.riskMoments),
        projection = calculateProjection(state.usageRange)
    )

    private fun restoreState(): OnboardingUiState {
        val name = savedStateHandle.get<String>(KEY_NAME).orEmpty()
        val usageRange = savedStateHandle.get<String>(KEY_USAGE_RANGE)
            ?.let { saved -> UsageRange.entries.firstOrNull { it.name == saved } }
            ?: OnboardingUiState.DEFAULT_USAGE_RANGE
        return withDerived(
            OnboardingUiState(
                name = name,
                nameError = if (name.all(::isNameCharacter)) null else NameError.INVALID_CHARACTERS,
                goals = savedStateHandle.get<ArrayList<String>>(KEY_GOALS).orEmpty()
                    .mapNotNull { saved -> Goal.entries.firstOrNull { it.name == saved } },
                usageRange = usageRange,
                riskMoments = savedStateHandle.get<ArrayList<String>>(KEY_RISK_MOMENTS).orEmpty()
                    .mapNotNullTo(linkedSetOf()) { saved -> RiskMoment.entries.firstOrNull { it.name == saved } },
                projection = calculateProjection(usageRange)
            )
        )
    }

    private fun isNameCharacter(char: Char): Boolean =
        char.isLetter() || char == ' ' || char in NAME_PUNCTUATION

    private companion object {
        const val KEY_NAME = "onboarding_name"
        const val KEY_GOALS = "onboarding_goals"
        const val KEY_USAGE_RANGE = "onboarding_usage_range"
        const val KEY_RISK_MOMENTS = "onboarding_risk_moments"

        // Nombres compuestos y con apóstrofo: "Ana-María", "O'Connor", "Mª."
        const val NAME_PUNCTUATION = "-'’.ªº"
    }
}
