package com.segundoaire.presentation.onboarding.legacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.usecase.CheckOverlayPermissionUseCase
import com.segundoaire.domain.usecase.CompleteOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LegacyOnboardingStep { WELCOME, PERMISSION }

data class LegacyOnboardingUiState(
    val step: LegacyOnboardingStep = LegacyOnboardingStep.WELCOME,
    val isOverlayPermissionGranted: Boolean = false
)

@HiltViewModel
class LegacyOnboardingViewModel @Inject constructor(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
    private val checkOverlayPermission: CheckOverlayPermissionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LegacyOnboardingUiState(isOverlayPermissionGranted = checkOverlayPermission())
    )
    val uiState: StateFlow<LegacyOnboardingUiState> = _uiState.asStateFlow()

    fun onWelcomeContinue() {
        _uiState.update { it.copy(step = LegacyOnboardingStep.PERMISSION) }
    }

    /** Se llama al volver de Ajustes (ON_RESUME) para releer el permiso. */
    fun refreshPermission() {
        val granted = checkOverlayPermission()
        _uiState.update { it.copy(isOverlayPermissionGranted = granted) }
    }

    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            completeOnboardingUseCase()
            onDone()
        }
    }
}
