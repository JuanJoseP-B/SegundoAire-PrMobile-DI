package com.segundoaire.presentation.setup

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

// No hay un ajuste público de un solo servicio de accesibilidad: se abre la lista
internal fun Context.openAccessibilitySettings() {
    startSettings(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
}

// Lleva directo al interruptor de esta app, no a la lista general
internal fun Context.openOverlaySettings() {
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
