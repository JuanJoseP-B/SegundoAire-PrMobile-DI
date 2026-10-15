package com.segundoaire.presentation.setup

/** Estado de los dos permisos que el motor necesita para mostrar la pausa. */
data class PermissionsUiState(
    val isAccessibilityGranted: Boolean = false,
    val isOverlayGranted: Boolean = false
) {
    /** "Continuar" solo se habilita con ambos; "Hacerlo después" no depende de esto. */
    val canContinue: Boolean get() = isAccessibilityGranted && isOverlayGranted
}
