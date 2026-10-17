package com.segundoaire.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.domain.repository.TreeRepository
import com.segundoaire.domain.repository.UserPreferencesRepository
import com.segundoaire.domain.usecase.CheckAccessibilityEnabledUseCase
import com.segundoaire.domain.usecase.CheckOverlayPermissionUseCase
import com.segundoaire.domain.usecase.GetUserProfileUseCase
import com.segundoaire.domain.usecase.MinutosPorDiaUseCase
import com.segundoaire.domain.usecase.SaveUserProfileUseCase
import com.segundoaire.presentation.onboarding.NameError
import com.segundoaire.presentation.panel.MissingPermission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/**
 * Perfil: el árbol con el nombre y las metas, "Plan y modo", sonidos y vibración y el
 * estado de los permisos. El nombre y las metas se editan en una hoja: el borrador
 * vive en [uiState]; lo guardado llega de DataStore y, mientras hay cambios sin
 * guardar, una actualización externa no pisa lo que el usuario escribe.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    getUserProfile: GetUserProfileUseCase,
    private val saveUserProfile: SaveUserProfileUseCase,
    trees: TreeRepository,
    private val preferences: UserPreferencesRepository,
    minutosPorDia: MinutosPorDiaUseCase,
    private val checkOverlayPermission: CheckOverlayPermissionUseCase,
    private val checkAccessibilityEnabled: CheckAccessibilityEnabledUseCase,
    clock: Clock
) : ViewModel() {

    private var saved: UserProfile? = null

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getUserProfile().collect { profile ->
                saved = profile
                _uiState.update { state ->
                    val keepDraft = !state.isLoading && state.hasChanges
                    withDerived(
                        state.copy(
                            isLoading = false,
                            name = if (keepDraft) state.name else profile.name,
                            goals = if (keepDraft) state.goals else profile.goals,
                            authMode = profile.authMode,
                            email = profile.email,
                            savedName = profile.name,
                            rigor = profile.suggestedRigor
                        )
                    )
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            trees.observeTree(LocalDate.now(clock)).collect { tree -> _uiState.update { it.copy(tree = tree) } }
        }
        viewModelScope.launch {
            preferences.isSoundEnabled
                .catch { emit(true) }
                .collect { enabled -> _uiState.update { it.copy(soundEnabled = enabled) } }
        }
        viewModelScope.launch {
            minutosPorDia(SHARE_RING_DAYS).collect { days -> _uiState.update { it.copy(rings = days) } }
        }
        onResume()
    }

    /** Al volver de Ajustes (ON_RESUME): relee los permisos. */
    fun onResume() {
        val missing = when {
            !checkAccessibilityEnabled() -> MissingPermission.ACCESSIBILITY
            !checkOverlayPermission() -> MissingPermission.OVERLAY
            else -> null
        }
        _uiState.update { it.copy(missingPermission = missing) }
    }

    /** "Sonidos y vibración": se aplica al momento. */
    fun onSoundChange(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferences.setSoundEnabled(enabled)
            } catch (_: IOException) {
                // No se pudo guardar: el interruptor sigue mostrando lo que de verdad rige
            }
        }
    }

    /** "Plan y modo": el rigor con el que arrancan los bloques. */
    fun onRigorChange(rigor: RigorLevel) {
        val current = saved ?: return
        if (current.suggestedRigor == rigor) return
        viewModelScope.launch {
            try {
                saveUserProfile(current.copy(suggestedRigor = rigor))
            } catch (_: IOException) {
                // Sin guardar, "Plan y modo" sigue mostrando el que rige
            }
        }
    }

    /** Al abrir la hoja "Editar": el borrador arranca en lo guardado. */
    fun onEditStart() {
        val profile = saved ?: return
        _uiState.update { withDerived(it.copy(name = profile.name, goals = profile.goals, nameError = null, feedback = null)) }
    }

    fun onNameChange(value: String) {
        val name = value.take(UserProfile.MAX_NAME_LENGTH)
        _uiState.update {
            withDerived(
                it.copy(
                    name = name,
                    nameError = if (UserProfile.isValidName(name)) null else NameError.INVALID_CHARACTERS,
                    feedback = null
                )
            )
        }
    }

    /** [goals] en el orden en que se eligieron; el primero es el principal. */
    fun onGoalsChange(goals: List<Goal>) {
        _uiState.update {
            withDerived(
                it.copy(goals = goals.distinct().take(UserProfile.MAX_GOALS), feedback = null)
            )
        }
    }

    fun onSave() {
        val state = _uiState.value
        val current = saved
        if (current == null || !state.canSave) return

        _uiState.update { it.copy(isSaving = true, feedback = null) }
        viewModelScope.launch {
            val feedback = try {
                saveUserProfile(current.copy(name = state.displayName, goals = state.goals))
                SaveFeedback.SAVED
            } catch (_: IOException) {
                // DataStore no pudo escribir: el borrador se conserva para reintentar
                SaveFeedback.FAILED
            }
            _uiState.update { withDerived(it.copy(isSaving = false, feedback = feedback)) }
        }
    }

    private companion object {
        const val SHARE_RING_DAYS = 12
    }

    // `hasChanges` compara el borrador normalizado con lo último guardado
    private fun withDerived(state: ProfileUiState): ProfileUiState {
        val profile = saved ?: return state
        return state.copy(
            hasChanges = state.displayName != profile.name || state.goals != profile.goals
        )
    }
}
