package com.segundoaire.presentation.blocks

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Bloques con su ViewModel. `collectAsStateWithLifecycle` deja de observar con
 * la pantalla detenida, y con eso se detiene también el tick del restante.
 */
@Composable
fun TimerRoute(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: TimerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    TimerScreen(
        state = state,
        onDurationSelected = viewModel::onDurationSelected,
        onRigorSelected = viewModel::onRigorSelected,
        onStart = viewModel::onStart,
        onEnd = viewModel::onEnd,
        modifier = modifier,
        contentPadding = contentPadding
    )
}
