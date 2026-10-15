package com.segundoaire.testing

import com.segundoaire.domain.model.InstalledApp
import com.segundoaire.domain.repository.InstalledAppsRepository

/** Devuelve [apps] tal cual, sin filtrar: el filtrado real se prueba en el repositorio. */
class FakeInstalledAppsRepository(var apps: List<InstalledApp> = emptyList()) : InstalledAppsRepository {
    var failNextQuery = false

    override suspend fun getLaunchableApps(): List<InstalledApp> {
        if (failNextQuery) {
            failNextQuery = false
            throw IllegalStateException("PackageManager no disponible")
        }
        return apps
    }
}
