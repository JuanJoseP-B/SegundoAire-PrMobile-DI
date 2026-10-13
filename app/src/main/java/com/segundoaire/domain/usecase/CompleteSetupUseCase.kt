package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.BlockedApp
import com.segundoaire.domain.model.InstalledApp
import com.segundoaire.domain.repository.BlockedAppRepository
import com.segundoaire.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Confirma el setup: [selected] pasa a ser la lista de apps vigiladas y el
 * perfil queda marcado como configurado.
 *
 * La selección reemplaza a la anterior (también a las apps de ejemplo que trae
 * una instalación nueva). Una app que ya estaba conserva su cooldown.
 * Primero se guardan las apps y después se marca el perfil: si algo falla en
 * medio, el setup sigue incompleto y repetirlo es seguro.
 */
class CompleteSetupUseCase @Inject constructor(
    private val blockedApps: BlockedAppRepository,
    private val profile: UserProfileRepository
) {
    suspend operator fun invoke(selected: List<InstalledApp>) {
        val previous = blockedApps.observeAll().first().associateBy { it.packageName }
        val apps = selected.distinctBy { it.packageName }.map { app ->
            previous[app.packageName]?.copy(appName = app.appName, isBlocked = true)
                ?: BlockedApp(packageName = app.packageName, appName = app.appName)
        }
        blockedApps.replaceAll(apps)
        profile.completeSetup()
    }
}
