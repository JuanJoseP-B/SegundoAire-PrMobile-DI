package com.segundoaire.presentation.setup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Pantalla de permisos con su ViewModel. Al volver de Ajustes (`ON_RESUME`)
 * relee el estado, así que el ítem concedido se marca solo.
 *
 * [onContinue] solo se dispara con ambos permisos; [onSkip] lleva igual al
 * Panel, que muestra el banner de permisos pendientes.
 */
@Composable
fun PermissionsRoute(
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PermissionsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    PermissionsScreen(
        state = state,
        onOpenAccessibilitySettings = { context.openAccessibilitySettings() },
        onOpenOverlaySettings = { context.openOverlaySettings() },
        onContinue = onContinue,
        onSkip = onSkip,
        modifier = modifier
    )
}
