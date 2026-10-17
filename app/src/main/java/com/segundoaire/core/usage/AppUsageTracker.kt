package com.segundoaire.core.usage

import android.util.Log
import com.segundoaire.di.ApplicationScope
import com.segundoaire.domain.model.AppUsageSession
import com.segundoaire.domain.repository.AppUsageRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/**
 * Estima cuánto se usa cada app vigilada con los eventos del servicio de accesibilidad,
 * sin `UsageStatsManager` ni permisos nuevos. Solo mide apps vigiladas.
 *
 * Todo se llama desde el hilo principal y fuera del camino crítico: aquí solo se toman
 * horas y se mueven datos en memoria. Los ratos cerrados se acumulan y se vuelcan a Room
 * por lotes, en [scope], al cambiar de app, al apagar la pantalla y cada 5 min con la app
 * abierta ([onPeriodicFlush]). Un rato con un hueco anormal se descarta (ver [UsageRules]).
 *
 * Sin `@Singleton`: vive lo que el servicio que lo recibe.
 */
class AppUsageTracker @Inject constructor(
    private val repository: AppUsageRepository,
    private val clock: Clock,
    @ApplicationScope private val scope: CoroutineScope
) {
    private var openPackage: String? = null
    private var openSince = 0L
    private val pending = mutableListOf<AppUsageSession>()

    // Lo ya guardado de hoy, para el GlassWall; el día se mueve al detectar uno nuevo
    private val today = MutableStateFlow(LocalDate.now(clock))

    @OptIn(ExperimentalCoroutinesApi::class)
    private val storedToday: StateFlow<Map<String, Long>> = today
        .flatMapLatest { day -> repository.observeUsageSince(day.atStartOfDay(clock.zone).toInstant().toEpochMilli()) }
        .catch { emit(emptyMap()) }
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    /**
     * Cambió la app en primer plano. Cierra el rato de la anterior y, si [packageName] es
     * vigilada y no la detuvo el GlassWall ([intercepted]), abre uno nuevo. Con
     * [packageName] `null` (SegundoAire, el inicio) solo cierra.
     */
    fun onForeground(packageName: String?, watched: Boolean, intercepted: Boolean) {
        if (packageName != null && packageName == openPackage) return
        closeOpen()
        if (packageName != null && watched && !intercepted) open(packageName)
        flush()
    }

    /** El usuario pulsó "Continuar": desde ahora sí está usando la app. */
    fun onContinue(packageName: String) {
        if (openPackage == packageName) return
        closeOpen()
        open(packageName)
        flush()
    }

    /** La pantalla se apagó: no hay uso aunque la app siga arriba. */
    fun onScreenOff() {
        closeOpen()
        flush()
    }

    /** Corte periódico: guarda lo que lleva el rato abierto y sigue midiendo desde ahora. */
    fun onPeriodicFlush() {
        val pkg = openPackage ?: return
        closeOpen()
        open(pkg)
        flush()
    }

    /** El servicio se va: cierra y guarda lo que haya. */
    fun stop() {
        closeOpen()
        flush()
    }

    /** Minutos estimados de hoy en [packageName]: lo guardado, lo pendiente y el rato abierto. */
    fun minutesToday(packageName: String): Int {
        val now = clock.millis()
        refreshDay()
        val stored = storedToday.value[packageName] ?: 0L
        val queued = pending.filter { it.packageName == packageName }.sumOf { it.durationMillis }
        val running = if (openPackage == packageName) (now - openSince).coerceIn(0L, UsageRules.MAX_SESSION_MILLIS) else 0L
        return ((stored + queued + running) / MILLIS_PER_MINUTE).toInt()
    }

    private fun open(packageName: String) {
        openPackage = packageName
        openSince = clock.millis()
    }

    private fun closeOpen() {
        val pkg = openPackage ?: return
        openPackage = null
        pending += UsageRules.close(pkg, openSince, clock.millis(), clock.zone)
    }

    private fun flush() {
        refreshDay()
        if (pending.isEmpty()) return
        val batch = pending.toList()
        pending.clear()
        scope.launch {
            try {
                repository.record(batch)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Perder un lote es perder minutos de una estimación, nunca tumbar el servicio
                Log.w(TAG, "No se pudo guardar el uso", e)
            }
        }
    }

    private fun refreshDay() {
        val now = LocalDate.now(clock)
        if (now != today.value) today.value = now
    }

    private companion object {
        const val TAG = "AppUsageTracker"
        const val MILLIS_PER_MINUTE = 60_000L
    }
}
