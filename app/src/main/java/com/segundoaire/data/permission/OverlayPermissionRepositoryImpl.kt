package com.segundoaire.data.permission

import android.content.Context
import android.provider.Settings
import com.segundoaire.domain.repository.OverlayPermissionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OverlayPermissionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : OverlayPermissionRepository {

    override fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(context)
}
