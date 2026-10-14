package com.segundoaire.presentation.blocks

import androidx.compose.runtime.Immutable
import com.segundoaire.domain.model.ActiveBlock
import com.segundoaire.domain.model.RigorLevel

/**
 * Estado único de Bloques. Con un bloque en curso, [selectedMinutes] y [rigor]
 * son los del bloque y [remainingMillis] lo que le falta según el reloj.
 *
 * [completedMinutes] es la duración del bloque que acaba de completarse y el usuario
 * aún no ha visto; mientras tenga valor la pantalla muestra el estado de completado.
 */
@Immutable
data class TimerUiState(
    val selectedMinutes: Int = ActiveBlock.DEFAULT_DURATION_MINUTES,
    val rigor: RigorLevel = RigorLevel.MINDFUL,
    val isRunning: Boolean = false,
    val remainingMillis: Long = selectedMinutes * ActiveBlock.MILLIS_PER_MINUTE,
    val watchedAppsCount: Int = 0,
    val completedMinutes: Int? = null
) {
    val totalMillis: Long get() = selectedMinutes * ActiveBlock.MILLIS_PER_MINUTE

    /** Segundos que muestra el anillo: redondea hacia arriba para no enseñar 00:00 con el bloque vivo. */
    val remainingSeconds: Int get() = ((remainingMillis + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND).toInt()

    /** 0f al iniciar, 1f al completar el bloque. */
    val progress: Float
        get() = when {
            completedMinutes != null -> 1f
            !isRunning -> 0f
            else -> (1f - remainingMillis.toFloat() / totalMillis).coerceIn(0f, 1f)
        }

    /** En Strict el bloque no se corta con un toque: terminar exige mantener presionado. */
    val endRequiresHold: Boolean get() = isRunning && rigor == RigorLevel.STRICT

    companion object {
        const val MILLIS_PER_SECOND = 1_000L
    }
}
