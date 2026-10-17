package com.segundoaire.core.service

import android.accessibilityservice.AccessibilityService
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.segundoaire.MainActivity
import com.segundoaire.R
import com.segundoaire.core.engine.InterceptorEngine
import com.segundoaire.core.profile.WeekLeavesCache
import com.segundoaire.core.usage.AppUsageTracker
import com.segundoaire.core.usage.UsageRules
import com.segundoaire.domain.model.GlassWallState
import com.segundoaire.domain.model.InterceptionIntent
import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.presentation.onboarding.sound.SoundManager
import com.segundoaire.presentation.overlay.GlassWallContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SegundoAireAccessibilityService : AccessibilityService(), OverlayWindowManager.Actions {

    @Inject lateinit var engine: InterceptorEngine

    // Se inyectan al crear el servicio: para la primera intercepción ya están en memoria
    @Inject lateinit var weekLeaves: WeekLeavesCache

    @Inject lateinit var recorder: InterceptionRecorder

    @Inject lateinit var usage: AppUsageTracker

    @Inject lateinit var sound: SoundManager

    private var overlay: OverlayWindowManager? = null
    private var lastPackage: String? = null
    private var interceptedPackage: String? = null
    private var debuggable = false
    private val handler = Handler(Looper.getMainLooper())

    // Con la pantalla apagada no hay uso, aunque la app siga arriba
    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) usage.onScreenOff()
        }
    }

    // Vuelca el rato abierto cada 5 min aunque no cambie la app
    private val periodicFlush = object : Runnable {
        override fun run() {
            usage.onPeriodicFlush()
            handler.postDelayed(this, UsageRules.FLUSH_INTERVAL_MILLIS)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        debuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        overlay = OverlayWindowManager(this, this, sound)
        engine.start()
        ContextCompat.registerReceiver(
            this,
            screenOffReceiver,
            IntentFilter(Intent.ACTION_SCREEN_OFF),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        handler.postDelayed(periodicFlush, UsageRules.FLUSH_INTERVAL_MILLIS)
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
        if (pkg == SYSTEM_UI) return
        if (pkg == packageName) {
            // Abrir SegundoAire cierra el rato de la app vigilada; el GlassWall no
            if (overlay?.isShowing != true) usage.onForeground(null, watched = false, intercepted = false)
            return
        }
        if (pkg == lastPackage) return
        lastPackage = pkg

        val intercept = engine.shouldIntercept(pkg)

        val elapsedMs = (SystemClock.elapsedRealtimeNanos() - start) / 1_000_000.0
        Log.d(TAG, "pkg=$pkg intercept=$intercept detección=${elapsedMs}ms")

        if (intercept) {
            interceptedPackage = pkg
            val added = overlay?.show(wallContent(pkg)) == true
            if (debuggable && added) logCriticalPath(event.eventTime, start)
            // Ya fuera del camino crítico: la ventana está agregada y la escritura va en segundo plano
            if (overlay?.isShowing == true) recorder.onShown(pkg)
            usage.onForeground(pkg, watched = true, intercepted = true)
        } else {
            interceptedPackage = null
            overlay?.hide()
            // Se fue a otra app sin pulsar "Continuar"
            recorder.onClosed(InterceptionResult.VOLVIO)
            usage.onForeground(pkg, watched = engine.isWatched(pkg), intercepted = false)
        }
    }

    // Todo de memoria: el estado del motor, el nombre de la app y las cifras ya calculadas
    private fun wallContent(pkg: String): GlassWallContent {
        weekLeaves.refresh()
        return GlassWallContent(
            state = engine.glassWallState(),
            appName = engine.appName(pkg),
            minutesToday = usage.minutesToday(pkg),
            weekLeaves = weekLeaves.leaves.value,
            cooldownMinutes = engine.cooldownMinutes(pkg)
        )
    }

    // Solo en debug: el presupuesto es de 50 ms entre el evento y la ventana ya agregada.
    // eventTime usa la base de uptimeMillis, así que incluye lo que tardó en llegar el evento.
    private fun logCriticalPath(eventTime: Long, callbackStartNanos: Long) {
        val sinceEventMs = SystemClock.uptimeMillis() - eventTime
        val inServiceMs = (SystemClock.elapsedRealtimeNanos() - callbackStartNanos) / 1_000_000.0
        Log.d(TAG, "camino crítico: evento→addView=${sinceEventMs}ms (en el servicio ${inServiceMs}ms)")
    }

    /**
     * "Volver" / "Volver a mi bloque": la app interceptada queda atrás y se abre el Panel,
     * donde brota la hoja nueva. Si la actividad no se puede abrir, se va al inicio.
     */
    override fun onBack() {
        recorder.onClosed(InterceptionResult.VOLVIO)
        overlay?.hide()
        interceptedPackage = null
        lastPackage = null
        try {
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .putExtra(MainActivity.EXTRA_OPEN_PANEL, true)
            )
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "No se pudo abrir el Panel; se vuelve al inicio", e)
            performGlobalAction(GLOBAL_ACTION_HOME)
        } catch (e: SecurityException) {
            Log.w(TAG, "Apertura del Panel bloqueada; se vuelve al inicio", e)
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onContinue() {
        // Strict no ofrece "Continuar"; el motor tiene la última palabra, no la UI
        val state = engine.glassWallState()
        if (state is GlassWallState.Strict) return
        recorder.onClosed(InterceptionResult.CONTINUO)
        interceptedPackage?.let { pkg ->
            engine.pause(pkg)
            usage.onContinue(pkg)
            // Sin regaños: un aviso breve sobre la app
            val text = if (state is GlassWallState.NoBlock) {
                getString(R.string.wall_toast_enjoy, engine.cooldownMinutes(pkg))
            } else {
                getString(R.string.wall_toast_block)
            }
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }
        overlay?.hide()
    }

    override fun onIntent(intent: InterceptionIntent) {
        recorder.onIntent(intent)
    }

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        // El servicio se va: no hay tiempo para la animación de salida
        overlay?.remove()
        overlay = null
        lastPackage = null
        interceptedPackage = null
        handler.removeCallbacks(periodicFlush)
        usage.stop()
        try {
            unregisterReceiver(screenOffReceiver)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Receptor ya quitado", e)
        }
        return super.onUnbind(intent)
    }

    private companion object {
        const val TAG = "SegundoAireService"
        const val SYSTEM_UI = "com.android.systemui"
    }
}
