package com.segundoaire.presentation.panel

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segundoaire.presentation.setup.openAccessibilitySettings
import com.segundoaire.presentation.setup.openOverlaySettings

/**
 * Panel con su ViewModel. Al volver de Ajustes (`ON_RESUME`) relee permisos y
 * saludo, así el banner desaparece solo cuando el permiso ya está concedido.
 *
 * [onEditApps] lleva a la selección de apps. [onTitleLongClick] solo se pasa en
 * debug, para abrir el catálogo de componentes.
 */
@Composable
fun PanelRoute(
    onEditApps: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    onTitleLongClick: (() -> Unit)? = null,
    viewModel: PanelViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

    PanelScreen(
        state = state,
        onToggleApp = viewModel::onToggleApp,
        onEditApps = onEditApps,
        onFixPermission = {
            // Abre el ajuste del permiso que falta, no la lista general
            when (state.missingPermission) {
                MissingPermission.ACCESSIBILITY -> context.openAccessibilitySettings()
                MissingPermission.OVERLAY -> context.openOverlaySettings()
                null -> Unit
            }
        },
        onNewMessage = viewModel::onNewMessage,
        onAppsAnimated = viewModel::onAppsAnimated,
        loadIcon = viewModel::loadIcon,
        modifier = modifier,
        contentPadding = contentPadding,
        onTitleLongClick = onTitleLongClick
    )
}
