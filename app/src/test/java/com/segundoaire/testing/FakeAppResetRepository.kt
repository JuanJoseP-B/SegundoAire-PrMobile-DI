package com.segundoaire.testing

import com.segundoaire.domain.repository.AppResetRepository
import java.io.IOException

/** Cuenta los reinicios; con [failNext] el siguiente falla como lo haría el disco. */
class FakeAppResetRepository : AppResetRepository {
    var clearCount = 0
        private set
    var failNext = false

    override suspend fun clearAll() {
        if (failNext) {
            failNext = false
            throw IOException("disco lleno")
        }
        clearCount++
    }
}
