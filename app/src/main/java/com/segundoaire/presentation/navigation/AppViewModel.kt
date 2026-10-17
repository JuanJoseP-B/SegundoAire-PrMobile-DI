package com.segundoaire.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.model.StartDestination
import com.segundoaire.domain.usecase.GetStartDestinationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    getStartDestination: GetStartDestinationUseCase
) : ViewModel() {

    /** `null` mientras se leen las preferencias y el perfil. */
    val startDestination: StateFlow<StartDestination?> = getStartDestination()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _openPanelRequest = MutableStateFlow<Long?>(null)
    private var requests = 0L

    /**
     * Pedido de mostrar el Panel ("Volver a mi bloque" del GlassWall); cambia con cada
     * pedido y vuelve a `null` cuando el contenedor lo atiende.
     */
    val openPanelRequest: StateFlow<Long?> = _openPanelRequest.asStateFlow()

    fun requestOpenPanel() {
        _openPanelRequest.value = ++requests
    }

    fun onOpenPanelHandled() {
        _openPanelRequest.value = null
    }
}
