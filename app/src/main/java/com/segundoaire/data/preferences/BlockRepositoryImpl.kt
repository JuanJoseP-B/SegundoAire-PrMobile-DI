package com.segundoaire.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.segundoaire.di.ApplicationScope
import com.segundoaire.domain.model.ActiveBlock
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.repository.BlockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bloque activo en memoria, respaldado en DataStore. Este repositorio es el único
 * que escribe esas claves, así que DataStore solo se lee una vez, al crearlo, para
 * recuperar un bloque que siguiera en curso cuando la app se cerró.
 *
 * Un bloque que llega a su fin deja guardada su duración hasta que el usuario la
 * ve: así el estado de completado aparece aunque el bloque venza con la app cerrada.
 */
@Singleton
class BlockRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val clock: Clock,
    @ApplicationScope scope: CoroutineScope
) : BlockRepository {

    private val activeBlock = MutableStateFlow<ActiveBlock?>(null)
    private val completedMinutes = MutableStateFlow<Int?>(null)

    // Ordena la restauración inicial con start/stop: gane quien gane, memoria y disco coinciden
    private val mutex = Mutex()

    init {
        scope.launch { mutex.withLock { restore() } }
    }

    override fun observeActiveBlock(): StateFlow<ActiveBlock?> = activeBlock.asStateFlow()

    override suspend fun start(durationMinutes: Int, rigor: RigorLevel) = mutex.withLock {
        val block = ActiveBlock.starting(clock.millis(), durationMinutes, rigor)
        dataStore.edit { prefs ->
            prefs[STARTED_AT] = block.startedAt
            prefs[ENDS_AT] = block.endsAt
            prefs[RIGOR] = block.rigor.name
            prefs[DURATION_MINUTES] = block.durationMinutes
            // Un bloque nuevo deja atrás el completado que no se llegó a ver
            prefs.remove(COMPLETED_MINUTES)
        }
        activeBlock.value = block
        completedMinutes.value = null
    }

    override suspend fun stop() = mutex.withLock {
        dataStore.edit { it.removeBlock() }
        activeBlock.value = null
    }

    // La Fase 15 registra aquí la sesión completada
    override suspend fun complete() = mutex.withLock {
        val block = activeBlock.value ?: return@withLock
        markCompleted(block)
        activeBlock.value = null
        completedMinutes.value = block.durationMinutes
    }

    override fun observeCompletedMinutes(): StateFlow<Int?> = completedMinutes.asStateFlow()

    override suspend fun acknowledgeCompleted() = mutex.withLock {
        dataStore.edit { it.remove(COMPLETED_MINUTES) }
        completedMinutes.value = null
    }

    private suspend fun markCompleted(block: ActiveBlock) {
        dataStore.edit { prefs ->
            prefs.removeBlock()
            prefs[COMPLETED_MINUTES] = block.durationMinutes
        }
    }

    /**
     * Carga lo guardado: el bloque si sigue en curso, o su duración como completado
     * si venció con la app cerrada. Lo corrupto se descarta.
     */
    private suspend fun restore() {
        val prefs = dataStore.data.first()
        val startedAt = prefs[STARTED_AT]
        val endsAt = prefs[ENDS_AT]
        val rigor = RigorLevel.entries.firstOrNull { it.name == prefs[RIGOR] }
        val durationMinutes = prefs[DURATION_MINUTES]
        completedMinutes.value = prefs[COMPLETED_MINUTES]
        if (startedAt == null && endsAt == null) return

        val block = if (startedAt != null && endsAt != null && rigor != null && durationMinutes != null) {
            ActiveBlock(startedAt, endsAt, rigor, durationMinutes)
        } else {
            null
        }
        when {
            block == null -> dataStore.edit { it.removeBlock() }
            block.isActiveAt(clock.millis()) -> activeBlock.value = block
            else -> {
                markCompleted(block)
                completedMinutes.value = block.durationMinutes
            }
        }
    }

    private fun MutablePreferences.removeBlock() {
        remove(STARTED_AT)
        remove(ENDS_AT)
        remove(RIGOR)
        remove(DURATION_MINUTES)
    }

    private companion object {
        val STARTED_AT = longPreferencesKey("block_started_at")
        val ENDS_AT = longPreferencesKey("block_ends_at")
        val RIGOR = stringPreferencesKey("block_rigor")
        val DURATION_MINUTES = intPreferencesKey("block_duration_minutes")
        val COMPLETED_MINUTES = intPreferencesKey("block_completed_minutes")
    }
}
