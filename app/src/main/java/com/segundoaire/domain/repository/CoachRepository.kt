package com.segundoaire.domain.repository

import com.segundoaire.domain.model.CoachMessage
import com.segundoaire.domain.model.CoachTopic

/**
 * El coach del Panel. Hoy los mensajes se generan en el dispositivo con las
 * estadísticas reales; la interfaz permite cambiarlo por un modelo remoto.
 */
interface CoachRepository {
    /**
     * Mensaje sobre [topic] o, sin tema, el siguiente de la rotación después de
     * [previous] (para "Otra sugerencia").
     */
    suspend fun message(topic: CoachTopic?, previous: CoachMessage?): CoachMessage
}
