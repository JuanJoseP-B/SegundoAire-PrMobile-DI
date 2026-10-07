package com.segundoaire.domain.usecase

import com.segundoaire.domain.repository.OverlayPermissionRepository
import javax.inject.Inject

class CheckOverlayPermissionUseCase @Inject constructor(
    private val repository: OverlayPermissionRepository
) {
    operator fun invoke(): Boolean = repository.canDrawOverlays()
}
