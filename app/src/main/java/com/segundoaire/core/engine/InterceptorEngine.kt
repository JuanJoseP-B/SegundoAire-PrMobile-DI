package com.segundoaire.core.engine

import android.os.SystemClock
import com.segundoaire.domain.model.ActiveBlock
import com.segundoaire.domain.repository.BlockRepository
import com.segundoaire.domain.usecase.GetBlockedAppsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/** Tiempo monótono desde el arranque; en producción, `SystemClock.elapsedRealtime()`. */
internal fun interface ElapsedRealtime {
    fun millis(): Long
}

/**
 * Decide si un paquete debe interceptarse. Camino crítico (<50 ms): solo lee un mapa
 * inmutable y el bloque activo, ambos en memoria (O(1)), sin E/S, corrutinas ni acceso
 * a Room. El mapa se reemplaza de forma atómica desde una corrutina que observa la base
 * de datos; el bloque es el valor actual del StateFlow del repositorio.
 */
@Singleton
class InterceptorEngine internal constructor(
    private val getBlockedApps: GetBlockedAppsUseCase,
    blockRepository: BlockRepository,
    private val clock: Clock,
    private val elapsedRealtime: ElapsedRealtime,
    private val scope: CoroutineScope
) {
    @Inject
    constructor(
        getBlockedApps: GetBlockedAppsUseCase,
        blockRepository: BlockRepository,
        clock: Clock
    ) : this(
        getBlockedApps,
        blockRepository,
        clock,
        ElapsedRealtime(SystemClock::elapsedRealtime),
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    )

    // paquete -> cooldown en ms, solo apps con isBlocked = true. Copia inmutable que se intercambia entera.
    @Volatile
    private var activeRules: Map<String, Long> = emptyMap()

    private val activeBlock: StateFlow<ActiveBlock?> = blockRepository.observeActiveBlock()

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
        // Durante un bloque se interceptan todas las apps vigiladas: el cooldown de "Continuar" no aplica
        if (isBlockActive()) return true
        val until = pausedUntil[packageName] ?: return true
        if (elapsedRealtime.millis() < until) return false
        pausedUntil.remove(packageName)
        return true
    }

    /** Cooldown tras pulsar "Continuar": evita el bucle de intercepción durante cooldownMinutes. */
    fun pause(packageName: String) {
        val cooldown = activeRules[packageName] ?: DEFAULT_PAUSE_MS
        pausedUntil[packageName] = elapsedRealtime.millis() + cooldown
    }

    // Se compara con el reloj porque nadie retira el bloque vencido mientras la app está cerrada
    private fun isBlockActive(): Boolean {
        val block = activeBlock.value ?: return false
        return block.isActiveAt(clock.millis())
    }

    private companion object {
        const val MS_PER_MINUTE = 60_000L
        const val DEFAULT_PAUSE_MS = 5 * MS_PER_MINUTE
    }
}
