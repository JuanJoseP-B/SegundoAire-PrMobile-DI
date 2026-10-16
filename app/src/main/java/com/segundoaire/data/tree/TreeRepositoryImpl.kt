package com.segundoaire.data.tree

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.domain.model.TreeState
import com.segundoaire.domain.model.weekStart
import com.segundoaire.domain.repository.InterceptionEventRepository
import com.segundoaire.domain.repository.TreeRepository
import com.segundoaire.domain.repository.UserPreferencesRepository
import com.segundoaire.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Arma el [TreeState] con lo que ya existe: las metas del perfil, las pausas respetadas
 * de la semana (las de ejemplo con el modo demo activo) y la fecha de plantado. Las hojas
 * ya vistas se guardan en DataStore junto con la semana a la que pertenecen.
 */
@Singleton
class TreeRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val profiles: UserProfileRepository,
    private val preferences: UserPreferencesRepository,
    private val interceptions: InterceptionEventRepository,
    private val installTime: InstallTimeSource,
    private val clock: Clock
) : TreeRepository {

    override fun observeTree(today: LocalDate): Flow<TreeState> {
        val weekStart = today.weekStart()
        val from = weekStart.atStartOfDay(clock.zone).toInstant().toEpochMilli()
        val seen = dataStore.data
            .map { prefs ->
                val storedWeek = prefs[SEEN_WEEK]?.let(LocalDate::ofEpochDay)
                TreeState.seenFor(storedWeek, prefs[SEEN_LEAVES] ?: 0, weekStart)
            }
            .distinctUntilChanged()
        val leaves = interceptions.observeSince(from)
            .map { events -> events.count { it.result == InterceptionResult.VOLVIO } }
            .distinctUntilChanged()
        return combine(profiles.profile, preferences.plantedAtMillis, leaves, seen) { profile, planted, week, seenLeaves ->
            TreeState(
                goals = profile.goals,
                weekLeaves = week,
                seenLeaves = seenLeaves,
                plantedAt = (planted ?: installTime.firstInstallMillis())?.toLocalDate()
            )
        }.distinctUntilChanged()
    }

    override suspend fun markLeavesSeen(today: LocalDate, count: Int) {
        dataStore.edit { prefs ->
            prefs[SEEN_WEEK] = today.weekStart().toEpochDay()
            prefs[SEEN_LEAVES] = count.coerceAtLeast(0)
        }
    }

    private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(clock.zone).toLocalDate()

    private companion object {
        val SEEN_WEEK = longPreferencesKey("tree_seen_week")
        val SEEN_LEAVES = intPreferencesKey("tree_seen_leaves")
    }
}
