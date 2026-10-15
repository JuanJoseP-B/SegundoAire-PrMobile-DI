package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.InstalledApp
import com.segundoaire.domain.repository.InstalledAppsRepository
import javax.inject.Inject

class GetInstalledAppsUseCase @Inject constructor(
    private val repository: InstalledAppsRepository
) {
    suspend operator fun invoke(): List<InstalledApp> = repository.getLaunchableApps()
}
