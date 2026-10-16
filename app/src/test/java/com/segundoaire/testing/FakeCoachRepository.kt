package com.segundoaire.testing

import com.segundoaire.domain.model.CoachMessage
import com.segundoaire.domain.model.CoachTopic
import com.segundoaire.domain.repository.CoachRepository

/** Coach que responde lo que el test le indique y anota cada pedido. */
class FakeCoachRepository(
    var reply: (CoachTopic?, CoachMessage?) -> CoachMessage = { _, _ -> CoachMessage.FirstSteps }
) : CoachRepository {
    val requests = mutableListOf<Pair<CoachTopic?, CoachMessage?>>()
    var failure: Exception? = null

    override suspend fun message(topic: CoachTopic?, previous: CoachMessage?): CoachMessage {
        requests += topic to previous
        failure?.let { throw it }
        return reply(topic, previous)
    }
}
