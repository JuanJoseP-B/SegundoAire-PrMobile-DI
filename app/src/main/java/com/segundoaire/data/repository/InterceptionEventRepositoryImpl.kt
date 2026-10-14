package com.segundoaire.data.repository

import com.segundoaire.data.local.dao.InterceptionEventDao
import com.segundoaire.data.local.entity.InterceptionEventEntity
import com.segundoaire.data.local.mapper.InterceptionEventMapper
import com.segundoaire.domain.model.InterceptionEvent
import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.domain.repository.InterceptionEventRepository
import com.segundoaire.domain.repository.UserPreferencesRepository
import com.segundoaire.domain.usecase.GenerarDatosDemoUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room guarda siempre las intercepciones reales, también con el modo demo activo.
 * En demo, las lecturas devuelven las de ejemplo; al apagarlo vuelven las reales.
 */
@Singleton
class InterceptionEventRepositoryImpl @Inject constructor(
    private val dao: InterceptionEventDao,
    private val preferences: UserPreferencesRepository,
    private val generateDemo: GenerarDatosDemoUseCase
) : InterceptionEventRepository {

    override suspend fun record(packageName: String, timestampMillis: Long): Long =
        dao.insert(
            InterceptionEventEntity(
                packageName = packageName,
                timestampMillis = timestampMillis,
                result = null
            )
        )

    override suspend fun setResult(id: Long, result: InterceptionResult) =
        dao.setResult(id, result.name)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeSince(fromMillis: Long): Flow<List<InterceptionEvent>> =
        preferences.isDemoMode.flatMapLatest { demo ->
            if (demo) {
                flowOf(
                    generateDemo().events
                        .filter { it.timestampMillis >= fromMillis }
                        .sortedBy { it.timestampMillis }
                )
            } else {
                dao.observeSince(fromMillis).map { list -> list.map(InterceptionEventMapper::toDomain) }
            }
        }
}
