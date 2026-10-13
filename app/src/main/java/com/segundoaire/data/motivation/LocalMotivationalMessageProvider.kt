package com.segundoaire.data.motivation

import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.provider.MotivationalMessageProvider
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Banco local de mensajes. Simula la espera de un modelo con [thinkingMillis],
 * así la pantalla ya resuelve el estado "pensando" que tendrá la IA real.
 *
 * [bank] devuelve los mensajes de un objetivo en el idioma actual; se pide en
 * cada llamada para que un cambio de idioma se note sin reiniciar.
 */
class LocalMotivationalMessageProvider(
    private val bank: (Goal?) -> List<String>,
    private val thinkingMillis: Long = DEFAULT_THINKING_MILLIS,
    private val random: Random = Random.Default
) : MotivationalMessageProvider {

    override suspend fun nextMessage(goal: Goal?, previous: String?): String {
        delay(thinkingMillis)
        return pickMessage(bank(goal), previous, random)
    }

    companion object {
        const val DEFAULT_THINKING_MILLIS = 800L
    }
}

/** Elige al azar de [bank], sin repetir [previous] mientras haya otra opción. */
internal fun pickMessage(bank: List<String>, previous: String?, random: Random): String {
    require(bank.isNotEmpty()) { "El banco de mensajes no puede estar vacío" }
    val candidates = bank.filter { it != previous }.ifEmpty { bank }
    return candidates[random.nextInt(candidates.size)]
}
