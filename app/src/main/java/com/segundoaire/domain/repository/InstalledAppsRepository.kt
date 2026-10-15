package com.segundoaire.domain.repository

import com.segundoaire.domain.model.InstalledApp

interface InstalledAppsRepository {

    /**
     * Apps que el usuario puede abrir desde el lanzador, sin la propia
     * SegundoAire y ordenadas por nombre. Cambia de hilo por su cuenta: se
     * puede llamar desde el hilo principal.
     */
    suspend fun getLaunchableApps(): List<InstalledApp>
}
