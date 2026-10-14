package com.segundoaire.data.repository

import com.segundoaire.data.local.dao.BlockSessionDao
import com.segundoaire.data.local.mapper.BlockSessionMapper
import com.segundoaire.domain.model.BlockSession
import com.segundoaire.domain.repository.BlockSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockSessionRepositoryImpl @Inject constructor(
    private val dao: BlockSessionDao
) : BlockSessionRepository {

    override suspend fun record(session: BlockSession) =
        dao.insert(BlockSessionMapper.toEntity(session))

    override fun observeCompletedSince(fromMillis: Long): Flow<List<BlockSession>> =
        dao.observeCompletedSince(fromMillis).map { list -> list.mapNotNull(BlockSessionMapper::toDomain) }
}
