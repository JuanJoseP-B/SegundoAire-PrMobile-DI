package com.segundoaire.presentation.panel

import androidx.compose.runtime.Immutable
import com.segundoaire.domain.model.BlockedApp
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.GreetingPeriod

/** Permiso que falta; el banner abre el ajuste exacto de cada uno. */
enum class MissingPermission { ACCESSIBILITY, OVERLAY }

/** Estado único del Panel: encabezado, dato de impacto, mensaje, apps y permisos. */
@Immutable
data class PanelUiState(
    /** `true` hasta que llegan las apps y el perfil; evita mostrar el estado vacío por error. */
    val isLoading: Boolean = true,
    val greeting: GreetingPeriod = GreetingPeriod.MORNING,
    val name: String = "",
    val mainGoal: Goal? = null,
    /** Minutos recuperados hoy. Lo alimenta la Fase 15; hasta entonces el Panel muestra las apps vigiladas. */
    val recoveredMinutesToday: Int = 0,
    val apps: List<BlockedApp> = emptyList(),
    /** Apps cuya entrada escalonada ya se mostró en esta sesión: no se vuelve a animar. */
    val animatedPackages: Set<String> = emptySet(),
    /** Último mensaje de la tarjeta de IA; `null` mientras no llegó el primero. */
    val message: String? = null,
    val isThinking: Boolean = false,
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

    val watchedAppsCount: Int get() = apps.count { it.isBlocked }
    val hasApps: Boolean get() = apps.isNotEmpty()
}
