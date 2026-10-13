package com.segundoaire.data.apps

/** Una actividad de lanzador tal como la devuelve el sistema. */
data class LauncherEntry(
    val packageName: String,
    val label: String
)

/**
 * Lo único que el repositorio necesita del `PackageManager`. Es una interfaz
 * para poder probar el filtrado sin un sistema Android detrás.
 */
interface LauncherAppSource {

    /** Paquete de esta misma app, que nunca debe ofrecerse para vigilar. */
    val ownPackageName: String

    /** Una entrada por actividad de lanzador: una app con varias aparece repetida. */
    suspend fun queryLauncherEntries(): List<LauncherEntry>
}
