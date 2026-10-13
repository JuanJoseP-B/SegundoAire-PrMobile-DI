package com.segundoaire.domain.provider

import com.segundoaire.domain.model.Goal

/**
 * Fuente de los mensajes de la tarjeta de IA del Panel. Hoy los resuelve un
 * banco local; la interfaz es asíncrona para que un modelo real pueda
 * reemplazarlo sin tocar la pantalla.
 */
interface MotivationalMessageProvider {
    /**
     * Un mensaje para quien persigue [goal] (`null` si aún no eligió uno). Si
     * hay más de uno disponible, nunca repite [previous].
     */
    suspend fun nextMessage(goal: Goal?, previous: String? = null): String
}
