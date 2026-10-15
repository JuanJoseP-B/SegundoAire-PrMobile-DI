package com.segundoaire.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.domain.usecase.GetUserProfileUseCase
import com.segundoaire.domain.usecase.SaveUserProfileUseCase
import com.segundoaire.presentation.onboarding.NameError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

/**
 * Perfil: nombre y objetivos editables, y la cuenta en solo lectura. El borrador
 * vive en [uiState]; lo guardado llega de DataStore y, mientras hay cambios sin
 * guardar, una actualización externa no pisa lo que el usuario escribe.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    getUserProfile: GetUserProfileUseCase,
    private val saveUserProfile: SaveUserProfileUseCase
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
                            email = profile.email
                        )
                    )
                }
            }
        }
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

    // `hasChanges` compara el borrador normalizado con lo último guardado
    private fun withDerived(state: ProfileUiState): ProfileUiState {
        val profile = saved ?: return state
        return state.copy(
            hasChanges = state.displayName != profile.name || state.goals != profile.goals
        )
    }
}
