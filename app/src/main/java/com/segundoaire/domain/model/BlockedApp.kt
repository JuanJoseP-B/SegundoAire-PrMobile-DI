package com.segundoaire.domain.model

data class BlockedApp(
    val packageName: String,
    val appName: String,
    val cooldownMinutes: Int = DEFAULT_COOLDOWN_MINUTES,
    val isBlocked: Boolean = true
) {
    companion object {
        const val DEFAULT_COOLDOWN_MINUTES = 5
    }
}
