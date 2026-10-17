package com.segundoaire.data.repository

import com.segundoaire.data.local.dao.AppUsageDao
import com.segundoaire.data.local.entity.AppUsageEntity
import com.segundoaire.domain.model.AppUsageSession
import com.segundoaire.domain.repository.AppUsageRepository
import com.segundoaire.domain.repository.BlockedAppRepository
import com.segundoaire.domain.repository.UserPreferencesRepository
import com.segundoaire.domain.usecase.GenerarDatosDemoUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uso de las apps vigiladas. Room guarda siempre lo medido, también con el modo demo
 * activo; en demo, las lecturas devuelven minutos de ejemplo para las apps vigiladas,
 * como las demás estadísticas.
 */
@Singleton
class AppUsageRepositoryImpl @Inject constructor(
    private val dao: AppUsageDao,
    private val preferences: UserPreferencesRepository,
    private val blockedApps: BlockedAppRepository
) : AppUsageRepository {

    override suspend fun record(sessions: List<AppUsageSession>) {
        if (sessions.isEmpty()) return
        dao.insertAll(sessions.map { AppUsageEntity(packageName = it.packageName, startedAt = it.startedAt, endedAt = it.endedAt) })
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeUsageSince(fromMillis: Long): Flow<Map<String, Long>> =
        preferences.isDemoMode.flatMapLatest { demo ->
            if (demo) {
                blockedApps.observeAll().map { apps ->
                    apps.mapIndexed { i, app -> app.packageName to GenerarDatosDemoUseCase.demoUsageMillis(i) }.toMap()
                }
            } else {
                dao.observeEndedSince(fromMillis).map { rows -> totalsSince(rows, fromMillis) }
            }
        }

    internal companion object {
        /** Suma por paquete; lo que empezó antes de [fromMillis] cuenta desde ahí. */
        fun totalsSince(rows: List<AppUsageEntity>, fromMillis: Long): Map<String, Long> =
            rows.groupBy { it.packageName }
                .mapValues { (_, list) -> list.sumOf { (it.endedAt - maxOf(it.startedAt, fromMillis)).coerceAtLeast(0L) } }
    }
}
