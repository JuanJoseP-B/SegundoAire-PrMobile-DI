package com.segundoaire.data.tree

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Cuándo se instaló la app: la fecha de plantado de las instalaciones de antes de la Fase 17. */
fun interface InstallTimeSource {
    fun firstInstallMillis(): Long?
}

class PackageInstallTimeSource @Inject constructor(
    @ApplicationContext private val context: Context
) : InstallTimeSource {
    override fun firstInstallMillis(): Long? = try {
        context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }
}
