package com.segundoaire.presentation.progress

import androidx.compose.runtime.Immutable
import com.segundoaire.domain.model.DayMinutes
import java.time.LocalDate

/** Un día de la semana en curso; [minutes] es `null` en los días que todavía no llegan. */
@Immutable
data class WeekDay(val date: LocalDate, val minutes: Int?) {
    val isFuture: Boolean get() = minutes == null
}

/**
 * Estado único de Progreso: el corte del tronco ([rings], un anillo por día del más
 * antiguo a hoy), la semana de lunes a domingo, el día elegido y las cuatro cifras.
 */
@Immutable
data class ProgressUiState(
    /** `true` hasta que llegan los datos. */
    val isLoading: Boolean = true,
    val today: LocalDate? = null,
    val rings: List<DayMinutes> = emptyList(),
    val week: List<WeekDay> = emptyList(),
    val selectedDay: LocalDate? = null,
    val weekMinutes: Int = 0,
    val weekOpenings: Int = 0,
    val bestStreak: Int = 0,
    val hardestHour: Int? = null,
    /** Se muestran datos de ejemplo, no el historial real. */
    val isDemo: Boolean = false
) {
    /** Minutos del día elegido; `null` si no hay un día elegido con datos. */
    val selectedMinutes: Int?
        get() = week.firstOrNull { it.date == selectedDay }?.minutes
            ?: rings.firstOrNull { it.date == selectedDay }?.minutes
}
