package com.segundoaire.presentation.profile

import androidx.compose.runtime.Immutable
import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.model.DayMinutes
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.TreeState
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.presentation.onboarding.NameError
import com.segundoaire.presentation.panel.MissingPermission

/** Resultado del último intento de guardar; se borra en cuanto el usuario vuelve a editar. */
enum class SaveFeedback { SAVED, FAILED }

/**
 * Estado único de Perfil. [name] y [goals] son el borrador que se edita en la hoja
 * "Editar"; [authMode] y [email] son de solo lectura (la cuenta es simulada).
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
    val feedback: SaveFeedback? = null,
    /** Lo que muestra la pantalla: el nombre guardado y el árbol (metas, hojas, plantado). */
    val savedName: String = "",
    val tree: TreeState = TreeState(),
    /** "Plan y modo": el rigor con el que arrancan los bloques nuevos. */
    val rigor: RigorLevel = RigorLevel.MINDFUL,
    val soundEnabled: Boolean = true,
    /** El primer permiso que falta, o `null` si están todos. */
    val missingPermission: MissingPermission? = null,
    /** Minutos de los últimos días, para los anillos de la imagen que se comparte. */
    val rings: List<DayMinutes> = emptyList()
) {
    /** Nombre tal como se guardaría. */
    val displayName: String get() = name.trim().take(UserProfile.MAX_NAME_LENGTH)

    val canSave: Boolean
        get() = !isLoading && hasChanges && !isSaving &&
            displayName.isNotEmpty() && nameError == null && goals.isNotEmpty()
}
