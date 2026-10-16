package com.segundoaire.presentation.panel

import androidx.compose.runtime.Immutable
import com.segundoaire.domain.model.ActiveBlock
import com.segundoaire.domain.model.CoachMessage
import com.segundoaire.domain.model.GreetingPeriod
import com.segundoaire.domain.model.TodayStats
import com.segundoaire.domain.model.TreeState

/** Permiso que falta; el banner abre el ajuste exacto de cada uno. */
enum class MissingPermission { ACCESSIBILITY, OVERLAY }

/**
 * La tarjeta del coach. Mientras [isThinking] muestra el brillo, los puntos y el
 * skeleton; después revela [message]. [revision] cambia con cada mensaje nuevo para
 * que el revelado empiece de nuevo aunque el texto se repita.
 */
@Immutable
data class CoachUiState(
    val message: CoachMessage? = null,
    val isThinking: Boolean = true,
    val revision: Int = 0
)

/**
 * Estado único del Panel: saludo, héroe con el árbol, cifras de hoy, bloque en curso,
 * coach y permisos pendientes. [hour] es la hora local con la que se eligen el saludo,
 * la luz del héroe y las luciérnagas.
 */
@Immutable
data class PanelUiState(
    val greeting: GreetingPeriod = GreetingPeriod.MORNING,
    val hour: Int = 9,
    val name: String = "",
    val stats: TodayStats = TodayStats(),
    val tree: TreeState = TreeState(),
    val activeBlock: ActiveBlock? = null,
    val coach: CoachUiState = CoachUiState(),
    val hasOverlayPermission: Boolean = true,
    val isAccessibilityEnabled: Boolean = true
) {
    /** El primero que falta; con accesibilidad apagada, la protección ni siquiera detecta apps. */
    val missingPermission: MissingPermission?
        get() = when {
            !isAccessibilityEnabled -> MissingPermission.ACCESSIBILITY
            !hasOverlayPermission -> MissingPermission.OVERLAY
            else -> null
        }
}

/** Lo que el Panel le pide al contenedor: cambiar de pestaña, abrir el bloque o mostrar un aviso. */
sealed interface PanelEffect {
    data object OpenBlocks : PanelEffect
    data object OpenProgress : PanelEffect

    /** Un bloque en curso: se abre su vista (el modo enfoque). */
    data object OpenActiveBlock : PanelEffect

    /** "Recuérdamelo": un aviso que no promete ninguna notificación. */
    data object RemindToast : PanelEffect
}
