package com.segundoaire.testing

import com.segundoaire.domain.model.BlockSession
import com.segundoaire.domain.repository.BlockSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Historial en memoria con la misma regla que Room: un `startedAt` repetido se ignora. */
class FakeBlockSessionRepository(initial: List<BlockSession> = emptyList()) : BlockSessionRepository {

    val sessions = MutableStateFlow(initial)

    override suspend fun record(session: BlockSession) {
        if (sessions.value.none { it.startedAt == session.startedAt }) {
            sessions.value = sessions.value + session
        }
    }

    override fun observeCompletedSince(fromMillis: Long): Flow<List<BlockSession>> =
        sessions.map { list ->
            list.filter { it.completed && it.endedAt >= fromMillis }.sortedBy { it.endedAt }
        }
}
