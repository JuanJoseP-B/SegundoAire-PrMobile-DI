package com.segundoaire.data.permission

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.segundoaire.core.service.SegundoAireAccessibilityService
import com.segundoaire.domain.repository.AccessibilityStatusRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccessibilityStatusRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AccessibilityStatusRepository {

    private val serviceId: String =
        ComponentName(context, SegundoAireAccessibilityService::class.java).flattenToString()

    override fun isServiceEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.split(':').any { it.equals(serviceId, ignoreCase = true) }
    }
}
