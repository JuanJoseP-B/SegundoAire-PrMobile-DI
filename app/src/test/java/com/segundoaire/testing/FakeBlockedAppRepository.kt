package com.segundoaire.testing

import com.segundoaire.domain.model.BlockedApp
import com.segundoaire.domain.repository.BlockedAppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException

/** Lista de apps vigiladas en memoria. Con [failNextReplace] el reemplazo falla y no cambia nada. */
class FakeBlockedAppRepository(initial: List<BlockedApp> = emptyList()) : BlockedAppRepository {
    var replaceCount = 0
    var failNextReplace = false

    private val apps = MutableStateFlow(initial)
    val current: List<BlockedApp> get() = apps.value

    override fun observeAll(): Flow<List<BlockedApp>> = apps

    override suspend fun getByPackage(packageName: String): BlockedApp? =
        apps.value.firstOrNull { it.packageName == packageName }

    override suspend fun upsert(app: BlockedApp) {
        apps.value = apps.value.filterNot { it.packageName == app.packageName } + app
    }

    override suspend fun setBlocked(packageName: String, isBlocked: Boolean) {
        apps.value = apps.value.map {
            if (it.packageName == packageName) it.copy(isBlocked = isBlocked) else it
        }
    }

    override suspend fun delete(packageName: String) {
        apps.value = apps.value.filterNot { it.packageName == packageName }
    }

    override suspend fun replaceAll(apps: List<BlockedApp>) {
        if (failNextReplace) {
            failNextReplace = false
            throw IOException("disco lleno")
        }
        this.apps.value = apps
        replaceCount++
    }
}
