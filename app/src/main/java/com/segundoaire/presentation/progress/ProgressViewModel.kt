package com.segundoaire.presentation.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.usecase.AperturasEvitadasUseCase
import com.segundoaire.domain.usecase.MinutosPorDiaUseCase
import com.segundoaire.domain.usecase.MinutosRecuperadosUseCase
import com.segundoaire.domain.usecase.ObserveDemoModeUseCase
import com.segundoaire.domain.usecase.RachaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/**
 * Progreso: minutos recuperados, aperturas evitadas, racha y la semana día a día,
 * en un único [ProgressUiState]. Con el modo demo activo, los mismos use cases
 * leen datos de ejemplo y el estado lo indica.
 */
@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val minutosRecuperados: MinutosRecuperadosUseCase,
    private val aperturasEvitadas: AperturasEvitadasUseCase,
    private val racha: RachaUseCase,
    private val minutosPorDia: MinutosPorDiaUseCase,
    private val observeDemoMode: ObserveDemoModeUseCase,
    private val clock: Clock
) : ViewModel() {

    // La semana se fija al pedir los datos: solo se vuelven a pedir si cambió el día
    private val today = MutableStateFlow(LocalDate.now(clock))

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ProgressUiState> = today
        .flatMapLatest {
            combine(
                minutosRecuperados(),
                aperturasEvitadas(),
                racha(),
                minutosPorDia(),
                observeDemoMode()
            ) { minutes, avoided, streak, week, demo ->
                ProgressUiState(
                    isLoading = false,
                    recoveredMinutes = minutes,
                    avoidedOpenings = avoided,
                    streakDays = streak,
                    week = week,
                    isDemo = demo
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ProgressUiState())

    /** Al volver a la pantalla (ON_RESUME): si pasó la medianoche, la semana avanza. */
    fun onResume() {
        today.value = LocalDate.now(clock)
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
