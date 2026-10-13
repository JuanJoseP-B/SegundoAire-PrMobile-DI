package com.segundoaire.presentation.setup

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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

// No hay un ajuste público de un solo servicio de accesibilidad: se abre la lista
private fun Context.openAccessibilitySettings() {
    startSettings(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
}

// Lleva directo al interruptor de esta app, no a la lista general
private fun Context.openOverlaySettings() {
    startSettings(
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
    )
}

private fun Context.startSettings(intent: Intent) {
    try {
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        // Algunos fabricantes retiran la pantalla: Ajustes general es mejor que nada
        startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
