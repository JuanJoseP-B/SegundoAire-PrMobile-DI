package com.segundoaire.presentation.profile

import androidx.compose.runtime.Immutable
import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.presentation.onboarding.NameError

/** Resultado del último intento de guardar; se borra en cuanto el usuario vuelve a editar. */
enum class SaveFeedback { SAVED, FAILED }

/**
 * Estado único de Perfil. [name] y [goals] son el borrador que se edita;
 * [authMode] y [email] son de solo lectura (la cuenta es simulada).
 */
@Immutable
data class ProfileUiState(
    /** `true` hasta que llega el perfil; evita mostrar campos vacíos por error. */
    val isLoading: Boolean = true,
    val name: String = "",
    val nameError: NameError? = null,
    val goals: List<Goal> = emptyList(),
    val authMode: AuthMode? = null,
    val email: String = "",
    /** El borrador difiere de lo guardado. */
    val hasChanges: Boolean = false,
    val isSaving: Boolean = false,
    val feedback: SaveFeedback? = null
) {
    /** Nombre tal como se guardaría. */
    val displayName: String get() = name.trim().take(UserProfile.MAX_NAME_LENGTH)

    val canSave: Boolean
        get() = !isLoading && hasChanges && !isSaving &&
            displayName.isNotEmpty() && nameError == null && goals.isNotEmpty()
}
