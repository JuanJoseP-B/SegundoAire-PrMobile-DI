package com.segundoaire.domain.model

/**
 * Cifras del Panel para hoy.
 *
 * - [recoveredMinutes]: minutos de los bloques completados hoy.
 * - [pausesRespected]: veces que el GlassWall terminó en "Volver".
 * - [openingsAvoided]: veces que el GlassWall interrumpió una apertura, se respetara o no.
 */
data class TodayStats(
    val recoveredMinutes: Int = 0,
    val pausesRespected: Int = 0,
    val openingsAvoided: Int = 0
)
