package com.segundoaire.core.engine

import android.os.SystemClock
import com.segundoaire.domain.usecase.GetBlockedAppsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Decide si un paquete debe interceptarse. Camino crítico (<50 ms): solo lee un mapa
 * inmutable en memoria (O(1)), sin E/S ni acceso a Room. El mapa se reemplaza de forma
 * atómica desde una corrutina que observa la base de datos.
 */
@Singleton
class InterceptorEngine @Inject constructor(
    private val getBlockedApps: GetBlockedAppsUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // paquete -> cooldown en ms, solo apps con isBlocked = true. Copia inmutable que se intercambia entera.
    @Volatile
    private var activeRules: Map<String, Long> = emptyMap()

    // paquete -> instante (elapsedRealtime) hasta el que no se intercepta. Solo hilo principal.
    private val pausedUntil = HashMap<String, Long>()

    private var started = false

    /** Idempotente: empieza a mantener la caché sincronizada con Room. */
    @Synchronized
    fun start() {
        if (started) return
        started = true
        getBlockedApps()
            .onEach { apps ->
                activeRules = apps.asSequence()
                    .filter { it.isBlocked }
                    .associate { it.packageName to it.cooldownMinutes * MS_PER_MINUTE }
            }
            .launchIn(scope)
    }

    fun shouldIntercept(packageName: String): Boolean {
        if (activeRules[packageName] == null) return false
        val until = pausedUntil[packageName] ?: return true
        if (SystemClock.elapsedRealtime() < until) return false
        pausedUntil.remove(packageName)
        return true
    }

    /** Cooldown tras pulsar "Continuar": evita el bucle de intercepción durante cooldownMinutes. */
    fun pause(packageName: String) {
        val cooldown = activeRules[packageName] ?: DEFAULT_PAUSE_MS
        pausedUntil[packageName] = SystemClock.elapsedRealtime() + cooldown
    }

    private companion object {
        const val MS_PER_MINUTE = 60_000L
        const val DEFAULT_PAUSE_MS = 5 * MS_PER_MINUTE
    }
}
