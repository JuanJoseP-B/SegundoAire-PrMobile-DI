package com.segundoaire.data.local.mapper

import com.segundoaire.data.local.entity.BlockSessionEntity
import com.segundoaire.domain.model.BlockSession
import com.segundoaire.domain.model.RigorLevel

object BlockSessionMapper {

    /** `null` si el rigor guardado no se reconoce: la fila se descarta. */
    fun toDomain(entity: BlockSessionEntity): BlockSession? {
        val rigor = RigorLevel.entries.firstOrNull { it.name == entity.rigor } ?: return null
        return BlockSession(
            startedAt = entity.startedAt,
            endedAt = entity.endedAt,
            durationMinutes = entity.durationMinutes,
            rigor = rigor,
            completed = entity.completed
        )
    }

    fun toEntity(model: BlockSession): BlockSessionEntity = BlockSessionEntity(
        startedAt = model.startedAt,
        endedAt = model.endedAt,
        durationMinutes = model.durationMinutes,
        rigor = model.rigor.name,
        completed = model.completed
    )
}
