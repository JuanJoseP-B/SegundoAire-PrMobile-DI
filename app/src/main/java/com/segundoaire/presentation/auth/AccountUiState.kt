package com.segundoaire.presentation.auth

import com.segundoaire.domain.model.AuthMode

/**
 * Estado de la pantalla de cuenta. [loadingMode] es el modo que se está
 * guardando; mientras tenga valor no se puede elegir otro.
 */
data class AccountUiState(
    val loadingMode: AuthMode? = null,
    val isCompleted: Boolean = false
) {
    val isBusy: Boolean get() = loadingMode != null

    /** Solo los proveedores muestran "Conectando…": seguir sin cuenta es inmediato. */
    val isConnecting: Boolean get() = loadingMode != null && loadingMode != AuthMode.GUEST
}
