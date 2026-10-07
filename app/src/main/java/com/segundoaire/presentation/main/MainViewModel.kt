package com.segundoaire.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.usecase.CheckAccessibilityEnabledUseCase
import com.segundoaire.domain.usecase.CheckOverlayPermissionUseCase
import com.segundoaire.domain.usecase.GetBlockedAppsUseCase
import com.segundoaire.domain.usecase.ToggleAppBlockStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getBlockedApps: GetBlockedAppsUseCase,
    private val toggleAppBlockStatus: ToggleAppBlockStatusUseCase,
    private val checkOverlayPermission: CheckOverlayPermissionUseCase,
    private val checkAccessibilityEnabled: CheckAccessibilityEnabledUseCase
) : ViewModel() {

    private data class Permissions(val overlay: Boolean, val accessibility: Boolean)

    private val permissions = MutableStateFlow(readPermissions())

    val uiState: StateFlow<MainUiState> = combine(
        getBlockedApps(),
        permissions
    ) { apps, perms ->
        MainUiState(
            hasOverlayPermission = perms.overlay,
            isAccessibilityEnabled = perms.accessibility,
            blockedApps = apps
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    /** Se llama al volver de Ajustes (ON_RESUME) para releer los permisos. */
    fun refreshPermissions() {
        permissions.update { readPermissions() }
    }

    fun onToggleApp(packageName: String, isBlocked: Boolean) {
        viewModelScope.launch { toggleAppBlockStatus(packageName, isBlocked) }
    }

    private fun readPermissions() = Permissions(
        overlay = checkOverlayPermission(),
        accessibility = checkAccessibilityEnabled()
    )
}
