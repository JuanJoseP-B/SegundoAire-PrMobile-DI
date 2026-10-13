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
 */
@Singleton
class BlockRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val clock: Clock,
    @ApplicationScope scope: CoroutineScope
) : BlockRepository {

    private val activeBlock = MutableStateFlow<ActiveBlock?>(null)

    // Ordena la restauración inicial con start/stop: gane quien gane, memoria y disco coinciden
    private val mutex = Mutex()

    init {
        scope.launch { mutex.withLock { activeBlock.value = restore() } }
    }

    override fun observeActiveBlock(): StateFlow<ActiveBlock?> = activeBlock.asStateFlow()

    override suspend fun start(durationMinutes: Int, rigor: RigorLevel) = mutex.withLock {
        val block = ActiveBlock.starting(clock.millis(), durationMinutes, rigor)
        dataStore.edit { prefs ->
            prefs[STARTED_AT] = block.startedAt
            prefs[ENDS_AT] = block.endsAt
            prefs[RIGOR] = block.rigor.name
            prefs[DURATION_MINUTES] = block.durationMinutes
        }
        activeBlock.value = block
    }

    override suspend fun stop() = clear()

    // Hoy termina igual que stop(); la Fase 15 registra aquí la sesión completada
    override suspend fun complete() = clear()

    private suspend fun clear() = mutex.withLock {
        dataStore.edit { it.removeBlock() }
        activeBlock.value = null
    }

    /** El bloque guardado, o `null` si no hay, está corrupto o venció con la app cerrada. */
    private suspend fun restore(): ActiveBlock? {
        val prefs = dataStore.data.first()
        val startedAt = prefs[STARTED_AT]
        val endsAt = prefs[ENDS_AT]
        val rigor = RigorLevel.entries.firstOrNull { it.name == prefs[RIGOR] }
        val durationMinutes = prefs[DURATION_MINUTES]
        if (startedAt == null && endsAt == null) return null

        val block = if (startedAt != null && endsAt != null && rigor != null && durationMinutes != null) {
            ActiveBlock(startedAt, endsAt, rigor, durationMinutes)
        } else {
            null
        }
        if (block == null || !block.isActiveAt(clock.millis())) {
            dataStore.edit { it.removeBlock() }
            return null
        }
        return block
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
    }
}
