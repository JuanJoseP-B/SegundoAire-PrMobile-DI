package com.segundoaire.data.local.mapper

import com.segundoaire.data.local.entity.BlockedAppEntity
import com.segundoaire.domain.model.BlockedApp

object BlockedAppMapper {

    fun toDomain(entity: BlockedAppEntity): BlockedApp = BlockedApp(
        packageName = entity.packageName,
        appName = entity.appName,
        cooldownMinutes = entity.cooldownMinutes,
        isBlocked = entity.isBlocked
    )

    fun toEntity(model: BlockedApp): BlockedAppEntity = BlockedAppEntity(
        packageName = model.packageName,
        appName = model.appName,
        cooldownMinutes = model.cooldownMinutes,
        isBlocked = model.isBlocked
    )
}
