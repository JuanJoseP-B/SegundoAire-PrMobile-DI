package com.segundoaire.data.repository

import com.segundoaire.data.local.dao.InterceptionEventDao
import com.segundoaire.data.local.entity.InterceptionEventEntity
import com.segundoaire.data.local.mapper.InterceptionEventMapper
import com.segundoaire.domain.model.InterceptionEvent
import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.domain.repository.InterceptionEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterceptionEventRepositoryImpl @Inject constructor(
    private val dao: InterceptionEventDao
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

    override fun observeSince(fromMillis: Long): Flow<List<InterceptionEvent>> =
        dao.observeSince(fromMillis).map { list -> list.map(InterceptionEventMapper::toDomain) }
}
