package com.segundoaire.testing

import com.segundoaire.domain.model.AppUsageSession
import com.segundoaire.domain.repository.AppUsageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Uso en memoria: el test fija los totales y revisa los lotes guardados. */
class FakeAppUsageRepository : AppUsageRepository {
    val usage = MutableStateFlow<Map<String, Long>>(emptyMap())
    val recorded = mutableListOf<AppUsageSession>()

    override suspend fun record(sessions: List<AppUsageSession>) {
        recorded += sessions
    }

    override fun observeUsageSince(fromMillis: Long): Flow<Map<String, Long>> = usage
}
