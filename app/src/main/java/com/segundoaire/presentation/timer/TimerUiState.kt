package com.segundoaire.presentation.timer

import androidx.compose.runtime.Immutable

enum class RigorLevel { MINDFUL, STRICT }

@Immutable
data class TimerUiState(
    val selectedMinutes: Int = DEFAULT_MINUTES,
    val rigorLevel: RigorLevel = RigorLevel.MINDFUL,
    val isRunning: Boolean = false,
    val remainingSeconds: Int = DEFAULT_MINUTES * 60,
    val watchedAppsCount: Int = 0
) {
    val totalSeconds: Int get() = selectedMinutes * 60

    /** 0f al iniciar, 1f al completar el bloque. */
    val progress: Float
        get() = if (!isRunning) 0f else 1f - remainingSeconds.toFloat() / totalSeconds

    companion object {
        const val DEFAULT_MINUTES = 25
        val DURATION_OPTIONS = listOf(25, 50, 90, 120)
    }
}
