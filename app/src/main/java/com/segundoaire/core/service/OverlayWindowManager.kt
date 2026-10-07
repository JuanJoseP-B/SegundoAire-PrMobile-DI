package com.segundoaire.core.service

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.segundoaire.presentation.overlay.GlassWall
import com.segundoaire.presentation.theme.SegundoAireTheme

/**
 * Aloja el "Muro de Cristal" en una ventana TYPE_APPLICATION_OVERLAY.
 * Debe usarse desde el hilo principal (los callbacks del AccessibilityService lo son).
 */
class OverlayWindowManager(
    private val context: Context,
    private val actions: Actions
) {
    interface Actions {
        fun onExit()
        fun onContinue()
    }

    private val windowManager = context.getSystemService(WindowManager::class.java)
    private var composeView: ComposeView? = null
    private var owner: OverlayLifecycleOwner? = null

    val isShowing: Boolean get() = composeView != null

    fun show() {
        if (composeView != null) return

        val lifecycleOwner = OverlayLifecycleOwner().also { it.start() }
        val view = ComposeView(context).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            // Anti-tapjacking: descarta toques si otra ventana oculta esta vista.
            filterTouchesWhenObscured = true
            setContent {
                SegundoAireTheme {
                    GlassWall(
                        onExit = actions::onExit,
                        onContinue = actions::onContinue
                    )
                }
            }
        }

        try {
            windowManager.addView(view, buildLayoutParams())
            composeView = view
            owner = lifecycleOwner
        } catch (e: Exception) {
            // Permiso revocado o ventana rechazada: no debe tumbar el servicio.
            Log.w(TAG, "No se pudo mostrar el overlay", e)
            lifecycleOwner.destroy()
        }
    }

    fun hide() {
        val view = composeView ?: return
        composeView = null
        try {
            windowManager.removeView(view)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Overlay ya removido", e)
        }
        owner?.destroy()
        owner = null
    }

    private fun buildLayoutParams(): WindowManager.LayoutParams {
        var flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags = flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
        }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                blurBehindRadius = BLUR_RADIUS_PX
            }
        }
    }

    private companion object {
        const val TAG = "OverlayWindowManager"
        const val BLUR_RADIUS_PX = 40
    }
}
