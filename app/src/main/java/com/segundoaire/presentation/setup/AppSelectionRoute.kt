package com.segundoaire.presentation.setup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Selección de apps con su ViewModel. [onCompleted] se llama una vez que la
 * selección quedó guardada y el setup marcado como completo.
 */
@Composable
fun AppSelectionRoute(
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppSelectionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnCompleted by rememberUpdatedState(onCompleted)

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) currentOnCompleted()
    }

    AppSelectionScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onToggle = viewModel::onToggle,
        onConfirm = viewModel::onConfirm,
        loadIcon = viewModel::loadIcon,
        modifier = modifier
    )
}
