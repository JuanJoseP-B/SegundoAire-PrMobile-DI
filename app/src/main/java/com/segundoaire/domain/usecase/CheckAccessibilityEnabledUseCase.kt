package com.segundoaire.domain.usecase

import com.segundoaire.domain.repository.AccessibilityStatusRepository
import javax.inject.Inject

class CheckAccessibilityEnabledUseCase @Inject constructor(
    private val repository: AccessibilityStatusRepository
) {
    operator fun invoke(): Boolean = repository.isServiceEnabled()
}
