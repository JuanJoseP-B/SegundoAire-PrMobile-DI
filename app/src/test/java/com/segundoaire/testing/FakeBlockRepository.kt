package com.segundoaire.testing

import com.segundoaire.domain.model.ActiveBlock
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.repository.BlockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Clock

/** Bloque activo en memoria; cuenta cómo terminó cada bloque para distinguir stop de complete. */
class FakeBlockRepository(
    private val clock: Clock,
    initial: ActiveBlock? = null,
    initialCompletedMinutes: Int? = null
) : BlockRepository {
    var stopCount = 0
    var completeCount = 0

    private val activeBlock = MutableStateFlow(initial)
    private val completedMinutes = MutableStateFlow(initialCompletedMinutes)

    override fun observeActiveBlock(): StateFlow<ActiveBlock?> = activeBlock

    override suspend fun start(durationMinutes: Int, rigor: RigorLevel) {
        activeBlock.value = ActiveBlock.starting(clock.millis(), durationMinutes, rigor)
        completedMinutes.value = null
    }

    override suspend fun stop() {
        stopCount++
        activeBlock.value = null
    }

    override suspend fun complete() {
        completeCount++
        completedMinutes.value = activeBlock.value?.durationMinutes
        activeBlock.value = null
    }

    override fun observeCompletedMinutes(): StateFlow<Int?> = completedMinutes

    override suspend fun acknowledgeCompleted() {
        completedMinutes.value = null
    }

    var forgetCount = 0

    override suspend fun forget() {
        forgetCount++
        activeBlock.value = null
        completedMinutes.value = null
    }
}
