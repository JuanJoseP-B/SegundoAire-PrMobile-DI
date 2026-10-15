package com.segundoaire.testing

import com.segundoaire.domain.repository.AccessibilityStatusRepository
import com.segundoaire.domain.repository.OverlayPermissionRepository

/** Permiso de accesibilidad que el test enciende o apaga como lo haría el usuario en Ajustes. */
class FakeAccessibilityStatusRepository(var enabled: Boolean = false) : AccessibilityStatusRepository {
    override fun isServiceEnabled(): Boolean = enabled
}

/** Permiso de superposición que el test concede o revoca. */
class FakeOverlayPermissionRepository(var granted: Boolean = false) : OverlayPermissionRepository {
    override fun canDrawOverlays(): Boolean = granted
}
