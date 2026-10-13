package com.segundoaire.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Consulta el lanzador con un intent MAIN/LAUNCHER. Desde Android 11 solo se ven
 * los paquetes que el manifest declara en `<queries>`: con ese mismo intent no
 * hace falta `QUERY_ALL_PACKAGES`, y las apps sin ícono de lanzador no aparecen.
 */
@Singleton
class PackageManagerLauncherAppSource @Inject constructor(
    @ApplicationContext private val context: Context
) : LauncherAppSource {

    override val ownPackageName: String = context.packageName

    override suspend fun queryLauncherEntries(): List<LauncherEntry> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        queryActivities(packageManager, intent).map { info ->
            LauncherEntry(
                packageName = info.activityInfo.packageName,
                label = info.loadLabel(packageManager).toString()
            )
        }
    }

    private fun queryActivities(packageManager: PackageManager, intent: Intent): List<ResolveInfo> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }
}
