package com.segundoaire.testing

import com.segundoaire.domain.model.TreeState
import com.segundoaire.domain.repository.TreeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

/** Árbol en memoria: el test fija las hojas y comprueba cuáles se marcaron como vistas. */
class FakeTreeRepository(initial: TreeState = TreeState()) : TreeRepository {
    val tree = MutableStateFlow(initial)
    val requestedDays = mutableListOf<LocalDate>()

    override fun observeTree(today: LocalDate): Flow<TreeState> {
        requestedDays += today
        return tree
    }

    override suspend fun markLeavesSeen(today: LocalDate, count: Int) {
        tree.update { it.copy(seenLeaves = count) }
    }
}
