package com.segundoaire.presentation.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** Un aviso breve del contenedor; [id] distingue dos avisos con el mismo texto. */
data class ShellToastMessage(val text: String, val id: Long)

/**
 * Estado del contenedor principal. Vive en la entrada `home` del NavHost: la pestaña
 * se guarda en su [SavedStateHandle] y sobrevive a la rotación y a la muerte del proceso.
 */
@HiltViewModel
class MainShellViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val selectedTab: StateFlow<MainTab> = savedStateHandle.getStateFlow(KEY_TAB, MainTab.PANEL)

    /** El modo enfoque está abierto (se ve si además hay un bloque en curso o recién completado). */
    val isFocusOpen: StateFlow<Boolean> = savedStateHandle.getStateFlow(KEY_FOCUS, false)

    private val _toast = MutableStateFlow<ShellToastMessage?>(null)
    val toast: StateFlow<ShellToastMessage?> = _toast.asStateFlow()
    private var toastCount = 0L

    fun selectTab(tab: MainTab) {
        savedStateHandle[KEY_TAB] = tab
    }

    fun openFocus() {
        savedStateHandle[KEY_FOCUS] = true
    }

    fun closeFocus() {
        savedStateHandle[KEY_FOCUS] = false
    }

    fun showToast(text: String) {
        _toast.value = ShellToastMessage(text, ++toastCount)
    }

    /** El aviso [id] ya terminó; uno más nuevo no se borra. */
    fun onToastDone(id: Long) {
        _toast.update { if (it?.id == id) null else it }
    }

    private companion object {
        const val KEY_TAB = "main_tab"
        const val KEY_FOCUS = "focus_open"
    }
}
