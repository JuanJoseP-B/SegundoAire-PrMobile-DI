package com.segundoaire.presentation.setup

import androidx.lifecycle.ViewModel
import com.segundoaire.domain.usecase.CheckAccessibilityEnabledUseCase
import com.segundoaire.domain.usecase.CheckOverlayPermissionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Permisos guiados. El sistema no avisa cuando el usuario concede un permiso
 * en Ajustes, así que la pantalla llama a [refresh] en cada `ON_RESUME`.
 */
@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val checkAccessibilityEnabled: CheckAccessibilityEnabledUseCase,
    private val checkOverlayPermission: CheckOverlayPermissionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(readPermissions())
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

    fun refresh() {
        _uiState.value = readPermissions()
    }

    private fun readPermissions() = PermissionsUiState(
        isAccessibilityGranted = checkAccessibilityEnabled(),
        isOverlayGranted = checkOverlayPermission()
    )
}
