package com.segundoaire.data.apps

import com.segundoaire.domain.model.InstalledApp
import com.segundoaire.domain.repository.InstalledAppsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstalledAppsRepositoryImpl @Inject constructor(
    private val source: LauncherAppSource
) : InstalledAppsRepository {

    override suspend fun getLaunchableApps(): List<InstalledApp> =
        source.queryLauncherEntries()
            .filter { it.packageName != source.ownPackageName }
            .distinctBy { it.packageName }
            .map { entry ->
                // Sin etiqueta legible, el paquete al menos distingue una app de otra
                InstalledApp(
                    packageName = entry.packageName,
                    appName = entry.label.trim().ifEmpty { entry.packageName }
                )
            }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.appName })
}
