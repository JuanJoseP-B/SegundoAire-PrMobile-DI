package com.segundoaire.presentation.setup

import com.segundoaire.domain.model.InstalledApp

/**
 * Estado de la selección de apps.
 *
 * Sin búsqueda, [suggested] y [apps] son las dos secciones (una app nunca está
 * en ambas). Con [query], [suggested] queda vacío y [apps] es una sola lista
 * con todas las coincidencias.
 */
data class AppSelectionUiState(
    val isLoading: Boolean = true,
    val hasInstalledApps: Boolean = false,
    val query: String = "",
    val suggested: List<InstalledApp> = emptyList(),
    val apps: List<InstalledApp> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,
    val saveFailed: Boolean = false
) {
    val isSearching: Boolean get() = query.isNotBlank()
    val selectedCount: Int get() = selectedPackages.size
    val hasResults: Boolean get() = suggested.isNotEmpty() || apps.isNotEmpty()

    /** Sin ninguna app elegida el setup no protege nada, así que no se confirma. */
    val canConfirm: Boolean
        get() = !isLoading && !isSaving && !isCompleted && selectedCount > 0
}
