package com.segundoaire.core.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.segundoaire.core.engine.InterceptorEngine
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SegundoAireAccessibilityService : AccessibilityService(), OverlayWindowManager.Actions {

    @Inject lateinit var engine: InterceptorEngine

    private var overlay: OverlayWindowManager? = null
    private var lastPackage: String? = null
    private var interceptedPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlay = OverlayWindowManager(this, this)
        engine.start()
        Log.d(TAG, "Servicio conectado")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val start = SystemClock.elapsedRealtimeNanos()

        // El paquete viene en el propio evento: no se necesita AccessibilityNodeInfo.
        // Si en el futuro se usa event.source / rootInActiveWindow, hay que llamar
        // a .recycle() inmediatamente después de usarlo.
        val pkg = event.packageName?.toString() ?: return
        // Ignorar nuestra propia ventana (overlay) y el SystemUI evita bucles y cierres falsos.
        if (pkg == packageName || pkg == SYSTEM_UI) return
        if (pkg == lastPackage) return
        lastPackage = pkg

        val intercept = engine.shouldIntercept(pkg)

        val elapsedMs = (SystemClock.elapsedRealtimeNanos() - start) / 1_000_000.0
        Log.d(TAG, "pkg=$pkg intercept=$intercept detección=${elapsedMs}ms")

        if (intercept) {
            interceptedPackage = pkg
            overlay?.show()
        } else {
            interceptedPackage = null
            overlay?.hide()
        }
    }

    override fun onExit() {
        overlay?.hide()
        interceptedPackage = null
        lastPackage = null
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    override fun onContinue() {
        interceptedPackage?.let(engine::pause)
        overlay?.hide()
    }

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        overlay?.hide()
        overlay = null
        lastPackage = null
        interceptedPackage = null
        return super.onUnbind(intent)
    }

    private companion object {
        const val TAG = "SegundoAireService"
        const val SYSTEM_UI = "com.android.systemui"
    }
}
