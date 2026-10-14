package com.segundoaire.core.service

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
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
import com.segundoaire.domain.model.Goal
import com.segundoaire.presentation.designsystem.theme.GlassBlurIntense
import com.segundoaire.presentation.designsystem.theme.LocalWindowBlurBehind
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.overlay.GlassWall
import com.segundoaire.presentation.overlay.GlassWallPhase
import java.util.function.Consumer
import kotlin.math.roundToInt

/**
 * Aloja el GlassWall en una ventana TYPE_APPLICATION_OVERLAY.
 * Debe usarse desde el hilo principal (los callbacks del AccessibilityService lo son).
 *
 * El fondo es `scrim` y, en Android 12+, el blur de la propia ventana
 * (`FLAG_BLUR_BEHIND`). Si el sistema desactiva el blur entre ventanas (ahorro de
 * batería, ajustes de desarrollador), queda solo el scrim.
 *
 * La vista se agrega primero y anima después: [show] no hace nada más que
 * `addView`, y la entrada arranca en el primer frame de la ventana. El radio del
 * blur sigue a la animación del fondo en [BLUR_STEPS] escalones, para no pedir un
 * `updateViewLayout` por cada frame.
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
    private val handler = Handler(Looper.getMainLooper())
    private val removeRunnable = Runnable { remove() }

    private var composeView: ComposeView? = null
    private var owner: OverlayLifecycleOwner? = null
    private var params: WindowManager.LayoutParams? = null
    private var blurListener: Consumer<Boolean>? = null

    // Estado de Compose: el vidrio de la hoja cambia de relleno según haya blur o no
    private var blurEnabled by mutableStateOf(false)
    private var phase by mutableStateOf(GlassWallPhase.Waiting)
    private var content by mutableStateOf(Content())
    private var backdropProgress = 0f

    val isShowing: Boolean get() = composeView != null

    /** [name] y [goal] personalizan el mensaje; llegan ya leídos de memoria. */
    fun show(name: String, goal: Goal?) {
        content = Content(name, goal)
        if (composeView != null) {
            // Otra intercepción mientras salía: vuelve a entrar desde donde iba
            if (phase == GlassWallPhase.Leaving) {
                handler.removeCallbacks(removeRunnable)
                phase = GlassWallPhase.Visible
            }
            return
        }

        blurEnabled = false
        phase = GlassWallPhase.Waiting
        backdropProgress = 0f
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
                        val current = content
                        GlassWall(
                            phase = phase,
                            name = current.name,
                            goal = current.goal,
                            onExit = actions::onExit,
                            onContinue = actions::onContinue,
                            modifier = Modifier.fillMaxSize(),
                            onBackdropProgress = ::onBackdropProgress,
                            onHidden = ::onHidden
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
        // La ventana ya está en pantalla: recién ahora se consulta el blur y se anima
        registerBlurListener()
        phase = GlassWallPhase.Visible
    }

    /** Sale con la animación y quita la ventana al terminar. */
    fun hide() {
        if (composeView == null || phase == GlassWallPhase.Leaving) return
        phase = GlassWallPhase.Leaving
        // Sin frames (pantalla apagada) la animación no avanza: la ventana se quita igual
        handler.postDelayed(removeRunnable, EXIT_TIMEOUT_MS)
    }

    /** Quita la ventana ya, sin animar. */
    fun remove() {
        handler.removeCallbacks(removeRunnable)
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
        phase = GlassWallPhase.Waiting
    }

    private fun onBackdropProgress(progress: Float) {
        backdropProgress = progress
        applyBlur()
    }

    // Llega desde la composición de la vista: no se puede quitar la ventana dentro de su propio frame
    private fun onHidden() {
        if (phase == GlassWallPhase.Leaving) handler.post(removeRunnable)
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

    // Lo llama la animación del fondo, una vez por frame como mucho; solo toca la ventana al cambiar de escalón
    private fun applyBlur() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val view = composeView ?: return
        val layoutParams = params ?: return
        val radius = if (blurEnabled) blurRadiusFor(backdropProgress) else 0
        if (radius == layoutParams.blurBehindRadius) return
        layoutParams.blurBehindRadius = radius
        try {
            windowManager.updateViewLayout(view, layoutParams)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Overlay ya removido", e)
        }
    }

    private fun blurRadiusFor(progress: Float): Int {
        val step = (progress.coerceIn(0f, 1f) * BLUR_STEPS).roundToInt()
        return blurRadiusPx * step / BLUR_STEPS
    }

    private data class Content(
        val name: String = "",
        val goal: Goal? = null
    )

    private companion object {
        const val TAG = "OverlayWindowManager"
        const val BLUR_STEPS = 16
        const val EXIT_TIMEOUT_MS = 1_000L
    }
}
