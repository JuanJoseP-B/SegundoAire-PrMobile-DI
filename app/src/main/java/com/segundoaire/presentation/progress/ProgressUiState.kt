package com.segundoaire.presentation.progress

import androidx.compose.runtime.Immutable
import com.segundoaire.domain.model.DayMinutes
import java.time.LocalDate

/** Estado único de Progreso: la semana que termina hoy. */
@Immutable
data class ProgressUiState(
    /** `true` hasta que llegan los datos; evita mostrar el estado vacío por error. */
    val isLoading: Boolean = true,
    val recoveredMinutes: Int = 0,
    val avoidedOpenings: Int = 0,
    val streakDays: Int = 0,
    /** Un elemento por día, del más antiguo a hoy. */
    val week: List<DayMinutes> = emptyList(),
    /** Se muestran datos de ejemplo, no el historial real. */
    val isDemo: Boolean = false
) {
    val today: LocalDate? get() = week.lastOrNull()?.date

    val hasData: Boolean get() = recoveredMinutes > 0 || avoidedOpenings > 0 || streakDays > 0
}
