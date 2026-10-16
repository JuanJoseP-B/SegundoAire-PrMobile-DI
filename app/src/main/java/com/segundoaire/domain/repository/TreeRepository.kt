package com.segundoaire.domain.repository

import com.segundoaire.domain.model.TreeState
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TreeRepository {
    /** El árbol en la semana que contiene [today]: metas, hojas, las ya vistas y la fecha de plantado. */
    fun observeTree(today: LocalDate): Flow<TreeState>

    /** Las [count] hojas de la semana de [today] ya brotaron en pantalla. */
    suspend fun markLeavesSeen(today: LocalDate, count: Int)
}
