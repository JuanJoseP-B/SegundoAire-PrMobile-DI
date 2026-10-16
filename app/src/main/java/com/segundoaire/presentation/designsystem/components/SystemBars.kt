package com.segundoaire.presentation.designsystem.components

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Mientras una superficie oscura cubre la barra de estado (el telón del onboarding, el
 * modo enfoque), la hora y la batería pasan a íconos claros; al dejar de estar [active]
 * (o salir de la composición) vuelven a lo que tenía el tema.
 */
@Composable
internal fun LightStatusBarIcons(active: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(view, active) {
        var context = view.context
        while (context is ContextWrapper && context !is Activity) context = context.baseContext
        val window = (context as? Activity)?.window
        if (window == null || !active) return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(window, view)
        val previous = controller.isAppearanceLightStatusBars
        controller.isAppearanceLightStatusBars = false
        onDispose { controller.isAppearanceLightStatusBars = previous }
    }
}
