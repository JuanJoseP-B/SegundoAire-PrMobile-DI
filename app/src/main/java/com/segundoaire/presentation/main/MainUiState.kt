package com.segundoaire.presentation.main

import androidx.compose.runtime.Immutable
import com.segundoaire.domain.model.BlockedApp

@Immutable
data class MainUiState(
    val hasOverlayPermission: Boolean = true,
    val isAccessibilityEnabled: Boolean = true,
    val blockedApps: List<BlockedApp> = emptyList(),
    val focusedMinutesToday: Int = 0
) {
    val needsPermissions: Boolean get() = !hasOverlayPermission || !isAccessibilityEnabled
    val watchedAppsCount: Int get() = blockedApps.count { it.isBlocked }
}
