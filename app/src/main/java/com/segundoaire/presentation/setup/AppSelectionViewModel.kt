package com.segundoaire.presentation.setup

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segundoaire.domain.model.InstalledApp
import com.segundoaire.domain.model.SuggestedApps
import com.segundoaire.domain.usecase.CompleteSetupUseCase
import com.segundoaire.domain.usecase.GetInstalledAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Selección de las apps a vigilar: carga las instaladas, ofrece primero las
 * sugeridas y, al confirmar, las guarda y marca el setup como completo.
 */
@HiltViewModel
class AppSelectionViewModel @Inject constructor(
    private val getInstalledApps: GetInstalledAppsUseCase,
    private val completeSetup: CompleteSetupUseCase,
    private val iconLoader: AppIconLoader
) : ViewModel() {

    private var installed: List<InstalledApp> = emptyList()

    private val _uiState = MutableStateFlow(AppSelectionUiState())
    val uiState: StateFlow<AppSelectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            installed = try {
                getInstalledApps()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Sin lista no hay qué elegir: la pantalla muestra el estado vacío
                emptyList()
            }
            _uiState.update { it.copy(isLoading = false, hasInstalledApps = installed.isNotEmpty()).withLists() }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query).withLists() }
    }

    fun onToggle(packageName: String, selected: Boolean) {
        _uiState.update { state ->
            if (state.isSaving || state.isCompleted) return@update state
            val packages = if (selected) state.selectedPackages + packageName else state.selectedPackages - packageName
            state.copy(selectedPackages = packages, saveFailed = false)
        }
    }

    fun onConfirm() {
        val state = _uiState.value
        if (!state.canConfirm) return

        _uiState.update { it.copy(isSaving = true, saveFailed = false) }
        viewModelScope.launch {
            val selected = installed.filter { it.packageName in state.selectedPackages }
            val saved = try {
                completeSetup(selected)
                true
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Room o DataStore no pudieron escribir: se conserva la selección para reintentar
                false
            }
            _uiState.update { it.copy(isSaving = false, isCompleted = saved, saveFailed = !saved) }
        }
    }

    /** Ícono de la fila cuando entra en pantalla; `null` mientras no haya o no exista. */
    suspend fun loadIcon(packageName: String): ImageBitmap? = iconLoader.load(packageName)

    private fun AppSelectionUiState.withLists(): AppSelectionUiState {
        val term = query.trim()
        return if (term.isEmpty()) {
            val (suggestedApps, others) = installed.partition { it.packageName in SuggestedApps.packageNames }
            copy(suggested = suggestedApps, apps = others)
        } else {
            copy(
                suggested = emptyList(),
                apps = installed.filter { it.appName.contains(term, ignoreCase = true) }
            )
        }
    }
}
