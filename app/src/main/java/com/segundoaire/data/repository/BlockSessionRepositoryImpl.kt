package com.segundoaire.data.repository

import com.segundoaire.data.local.dao.BlockSessionDao
import com.segundoaire.data.local.mapper.BlockSessionMapper
import com.segundoaire.domain.model.BlockSession
import com.segundoaire.domain.repository.BlockSessionRepository
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
 * Room guarda siempre los bloques reales. Con el modo demo activo, las lecturas
 * devuelven los de ejemplo, filtrados igual que la consulta real; al apagarlo
 * vuelve el historial real tal como estaba.
 */
@Singleton
class BlockSessionRepositoryImpl @Inject constructor(
    private val dao: BlockSessionDao,
    private val preferences: UserPreferencesRepository,
    private val generateDemo: GenerarDatosDemoUseCase
) : BlockSessionRepository {

    override suspend fun record(session: BlockSession) =
        dao.insert(BlockSessionMapper.toEntity(session))

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCompletedSince(fromMillis: Long): Flow<List<BlockSession>> =
        preferences.isDemoMode.flatMapLatest { demo ->
            if (demo) {
                flowOf(
                    generateDemo().sessions
                        .filter { it.completed && it.endedAt >= fromMillis }
                        .sortedBy { it.endedAt }
                )
            } else {
                dao.observeCompletedSince(fromMillis)
                    .map { list -> list.mapNotNull(BlockSessionMapper::toDomain) }
            }
        }
}
