package com.segundoaire.domain.usecase

import com.segundoaire.domain.repository.BlockedAppRepository
import javax.inject.Inject

class ToggleAppBlockStatusUseCase @Inject constructor(
    private val repository: BlockedAppRepository
) {
    suspend operator fun invoke(packageName: String, isBlocked: Boolean) =
        repository.setBlocked(packageName, isBlocked)
}
