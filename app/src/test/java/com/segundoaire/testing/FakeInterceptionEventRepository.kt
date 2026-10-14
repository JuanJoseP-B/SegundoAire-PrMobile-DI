package com.segundoaire.testing

import com.segundoaire.domain.model.InterceptionEvent
import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.domain.repository.InterceptionEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Intercepciones en memoria con ids autoincrementales, como en Room. */
class FakeInterceptionEventRepository(
    initial: List<InterceptionEvent> = emptyList()
) : InterceptionEventRepository {

    val events = MutableStateFlow(initial)

    override suspend fun record(packageName: String, timestampMillis: Long): Long {
        val id = (events.value.maxOfOrNull { it.id } ?: 0L) + 1
        events.value = events.value + InterceptionEvent(id, packageName, timestampMillis, result = null)
        return id
    }

    override suspend fun setResult(id: Long, result: InterceptionResult) {
        events.value = events.value.map { if (it.id == id) it.copy(result = result) else it }
    }

    override fun observeSince(fromMillis: Long): Flow<List<InterceptionEvent>> =
        events.map { list -> list.filter { it.timestampMillis >= fromMillis }.sortedBy { it.timestampMillis } }
}
