package com.segundoaire.core.service

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.WindowManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.segundoaire.presentation.designsystem.theme.GlassBlurIntense
import com.segundoaire.presentation.designsystem.theme.LocalWindowBlurBehind
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.overlay.GlassWall
import java.util.function.Consumer
import kotlin.math.roundToInt

/**
 * Aloja el GlassWall en una ventana TYPE_APPLICATION_OVERLAY.
 * Debe usarse desde el hilo principal (los callbacks del AccessibilityService lo son).
 *
 * El fondo es `scrim` y, en Android 12+, el blur de la propia ventana
 * (`FLAG_BLUR_BEHIND`). Si el sistema desactiva el blur entre ventanas (ahorro de
 * batería, ajustes de desarrollador), queda solo el scrim.
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
    private val blurRadiusPx =
        (GlassBlurIntense.value * context.resources.displayMetrics.density).roundToInt()

    private var composeView: ComposeView? = null
    private var owner: OverlayLifecycleOwner? = null
    private var params: WindowManager.LayoutParams? = null
    private var blurListener: Consumer<Boolean>? = null

    // Estado de Compose: el vidrio de la hoja cambia de relleno según haya blur o no
    private var blurEnabled by mutableStateOf(false)

    val isShowing: Boolean get() = composeView != null

    fun show() {
        if (composeView != null) return

        blurEnabled = false
        val lifecycleOwner = OverlayLifecycleOwner().also { it.start() }
        val view = ComposeView(context).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            // Anti-tapjacking: descarta toques si otra ventana oculta esta vista.
            filterTouchesWhenObscured = true
            setContent {
                SegundoAireTheme {
                    CompositionLocalProvider(LocalWindowBlurBehind provides blurEnabled) {
                        GlassWall(
                            onExit = actions::onExit,
                            onContinue = actions::onContinue,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
        val layoutParams = buildLayoutParams()

        try {
            windowManager.addView(view, layoutParams)
        } catch (e: Exception) {
            // Permiso revocado o ventana rechazada: no debe tumbar el servicio.
            Log.w(TAG, "No se pudo mostrar el overlay", e)
            lifecycleOwner.destroy()
            return
        }
        composeView = view
        owner = lifecycleOwner
        params = layoutParams
        // La ventana ya está en pantalla: recién ahora se consulta el blur
        registerBlurListener()
    }

    fun hide() {
        val view = composeView ?: return
        composeView = null
        params = null
        unregisterBlurListener()
        try {
            windowManager.removeView(view)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Overlay ya removido", e)
        }
        owner?.destroy()
        owner = null
    }

    // La ventana nace sin blur: el radio lo pone applyBlur() cuando el sistema confirma que hay
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
        )
    }

    // El sistema llama al listener al registrarlo, con el estado actual, y en cada cambio
    private fun registerBlurListener() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val listener = Consumer<Boolean> { enabled ->
            blurEnabled = enabled
            applyBlur()
        }
        blurListener = listener
        windowManager.addCrossWindowBlurEnabledListener(listener)
    }

    private fun unregisterBlurListener() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        blurListener?.let(windowManager::removeCrossWindowBlurEnabledListener)
        blurListener = null
    }

    private fun applyBlur() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val view = composeView ?: return
        val layoutParams = params ?: return
        val radius = if (blurEnabled) blurRadiusPx else 0
        if (radius == layoutParams.blurBehindRadius) return
        layoutParams.blurBehindRadius = radius
        try {
            windowManager.updateViewLayout(view, layoutParams)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Overlay ya removido", e)
        }
    }

    private companion object {
        const val TAG = "OverlayWindowManager"
    }
}
