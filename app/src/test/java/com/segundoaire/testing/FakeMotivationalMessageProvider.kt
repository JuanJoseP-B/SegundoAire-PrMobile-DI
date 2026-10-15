package com.segundoaire.testing

import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.provider.MotivationalMessageProvider
import kotlinx.coroutines.CompletableDeferred
import java.io.IOException

/**
 * Proveedor de mensajes en memoria: entrega [messages] en orden y registra cada
 * llamada. Con [gate] la respuesta espera a que el test la libere, y con
 * [failNext] la siguiente falla como lo haría un modelo remoto.
 */
class FakeMotivationalMessageProvider(
    messages: List<String> = listOf("uno", "dos", "tres")
) : MotivationalMessageProvider {
    private val queue = ArrayDeque(messages)

    val calls = mutableListOf<Pair<Goal?, String?>>()
    var gate: CompletableDeferred<Unit>? = null
    var failNext = false

    override suspend fun nextMessage(goal: Goal?, previous: String?): String {
        calls += goal to previous
        gate?.await()
        if (failNext) {
            failNext = false
            throw IOException("sin conexión")
        }
        return queue.removeFirst()
    }
}
