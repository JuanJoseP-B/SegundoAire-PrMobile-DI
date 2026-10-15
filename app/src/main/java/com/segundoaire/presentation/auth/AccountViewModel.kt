package com.segundoaire.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.usecase.SaveAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

/**
 * Cuenta simulada (SEGUNDOAIRE_SPEC.md §5): no hay backend, así que elegir un
 * proveedor solo espera un momento y guarda el modo de acceso en el perfil.
 */
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val saveAccount: SaveAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    /**
     * Guarda [mode] y cierra la pantalla, que navega al ver `isCompleted`.
     * [AuthMode.EMAIL] no pasa por aquí: se guarda al enviar su formulario.
     */
    fun onAuthModeSelected(mode: AuthMode) {
        val state = _uiState.value
        if (mode == AuthMode.EMAIL || state.isBusy || state.isCompleted) return

        _uiState.update { it.copy(loadingMode = mode) }
        viewModelScope.launch {
            if (mode != AuthMode.GUEST) delay(PROVIDER_DELAY_MILLIS)
            val saved = try {
                saveAccount(mode)
                true
            } catch (_: IOException) {
                // DataStore no pudo escribir: los botones vuelven a quedar disponibles para reintentar
                false
            }
            _uiState.update { it.copy(loadingMode = null, isCompleted = saved) }
        }
    }

    companion object {
        /** Lo que tarda la conexión simulada con un proveedor. */
        const val PROVIDER_DELAY_MILLIS = 1200L
    }
}
