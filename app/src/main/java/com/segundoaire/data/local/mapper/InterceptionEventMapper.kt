package com.segundoaire.data.local.mapper

import com.segundoaire.data.local.entity.InterceptionEventEntity
import com.segundoaire.domain.model.InterceptionEvent
import com.segundoaire.domain.model.InterceptionIntent
import com.segundoaire.domain.model.InterceptionResult

object InterceptionEventMapper {

    // Un resultado que no se reconoce cuenta como sin resolver
    fun toDomain(entity: InterceptionEventEntity): InterceptionEvent = InterceptionEvent(
        id = entity.id,
        packageName = entity.packageName,
        timestampMillis = entity.timestampMillis,
        result = InterceptionResult.entries.firstOrNull { it.name == entity.result },
        intent = InterceptionIntent.entries.firstOrNull { it.name == entity.intent }
    )
}
