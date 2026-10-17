package com.segundoaire.core.profile

import com.segundoaire.di.ApplicationScope
import com.segundoaire.domain.repository.TreeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hojas del árbol esta semana, en memoria para el GlassWall ("Tu árbol lleva n hojas esta
 * semana"). Se observa desde que se crea (al conectar el servicio); al mostrar el muro solo
 * se lee [leaves].
 */
@Singleton
class WeekLeavesCache @Inject constructor(
    trees: TreeRepository,
    private val clock: Clock,
    @ApplicationScope scope: CoroutineScope
) {
    private val today = MutableStateFlow(LocalDate.now(clock))

    @OptIn(ExperimentalCoroutinesApi::class)
    val leaves: StateFlow<Int> = today
        .flatMapLatest { day -> trees.observeTree(day).map { it.weekLeaves } }
        .catch { emit(0) }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    /** Si cambió el día, la semana se vuelve a pedir. Barato: solo compara una fecha. */
    fun refresh() {
        val now = LocalDate.now(clock)
        if (now != today.value) today.value = now
    }
}
