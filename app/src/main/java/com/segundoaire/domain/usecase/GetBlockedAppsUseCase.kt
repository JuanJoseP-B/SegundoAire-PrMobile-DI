package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.BlockedApp
import com.segundoaire.domain.repository.BlockedAppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBlockedAppsUseCase @Inject constructor(
    private val repository: BlockedAppRepository
) {
    operator fun invoke(): Flow<List<BlockedApp>> = repository.observeAll()
}
