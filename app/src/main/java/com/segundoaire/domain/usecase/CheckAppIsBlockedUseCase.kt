package com.segundoaire.domain.usecase

import com.segundoaire.domain.repository.BlockedAppRepository
import javax.inject.Inject

class CheckAppIsBlockedUseCase @Inject constructor(
    private val repository: BlockedAppRepository
) {
    suspend operator fun invoke(packageName: String): Boolean =
        repository.getByPackage(packageName)?.isBlocked ?: false
}
