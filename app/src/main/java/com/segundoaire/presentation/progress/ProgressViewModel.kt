package com.segundoaire.presentation.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.repository.TreeRepository
import com.segundoaire.domain.usecase.AperturasEvitadasUseCase
import com.segundoaire.domain.usecase.HoraMasDificilUseCase
import com.segundoaire.domain.usecase.MejorRachaUseCase
import com.segundoaire.domain.usecase.MinutosPorDiaUseCase
import com.segundoaire.domain.usecase.ObserveDemoModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Progreso: el corte del tronco (un anillo por día desde que se plantó el árbol, hasta
 * 12), la semana de lunes a domingo, el día elegido y las cuatro cifras de la semana.
 * Con el modo demo activo, los mismos casos de uso leen datos de ejemplo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val minutosPorDia: MinutosPorDiaUseCase,
    private val aperturasEvitadas: AperturasEvitadasUseCase,
    private val mejorRacha: MejorRachaUseCase,
    private val horaMasDificil: HoraMasDificilUseCase,
    private val observeDemoMode: ObserveDemoModeUseCase,
    private val trees: TreeRepository,
    private val clock: Clock
) : ViewModel() {

    private data class Figures(val openings: Int, val bestStreak: Int, val hardestHour: Int?, val isDemo: Boolean)

    // La semana se fija al pedir los datos: solo se vuelven a pedir si cambió el día
    private val today = MutableStateFlow(LocalDate.now(clock))
    private val selected = MutableStateFlow<LocalDate?>(null)

    val uiState: StateFlow<ProgressUiState> = today
        .flatMapLatest { day ->
            // Lunes = 1 … domingo = 7: los días de la semana que ya pasaron, hoy incluido
            val daysThisWeek = day.dayOfWeek.value
            val figures = combine(
                aperturasEvitadas(daysThisWeek),
                mejorRacha(),
                horaMasDificil(),
                observeDemoMode()
            ) { openings, streak, hour, demo -> Figures(openings, streak, hour, demo) }

            combine(minutosPorDia(ProgressRules.MAX_RINGS), figures, trees.observeTree(day), selected) { days, figures, tree, selection ->
                val daysSincePlanted = tree.plantedAt?.let { ChronoUnit.DAYS.between(it, day).toInt().coerceAtLeast(0) }
                val past = days.takeLast(daysThisWeek).map { WeekDay(it.date, it.minutes) }
                val future = (1..DayOfWeek.SUNDAY.value - daysThisWeek).map { WeekDay(day.plusDays(it.toLong()), null) }
                ProgressUiState(
                    isLoading = false,
                    today = day,
                    rings = days.takeLast(ProgressRules.ringCount(daysSincePlanted, daysThisWeek)),
                    week = past + future,
                    selectedDay = selection?.takeIf { it in past.map(WeekDay::date) } ?: day,
                    weekMinutes = past.sumOf { it.minutes ?: 0 },
                    weekOpenings = figures.openings,
                    bestStreak = figures.bestStreak,
                    hardestHour = figures.hardestHour,
                    isDemo = figures.isDemo
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ProgressUiState())

    /** Toca un día de la semana: se ilumina su anillo. Los días que no llegan no se eligen. */
    fun onDaySelected(date: LocalDate) {
        val day = uiState.value.week.firstOrNull { it.date == date } ?: return
        if (!day.isFuture) selected.value = date
    }

    /** Al volver a la pantalla (ON_RESUME): si pasó la medianoche, la semana avanza. */
    fun onResume() {
        today.value = LocalDate.now(clock)
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
