package com.segundoaire.presentation.profile

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Perfil con su ViewModel. */
@Composable
fun ProfileRoute(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onGoalsChange = viewModel::onGoalsChange,
        onSave = viewModel::onSave,
        modifier = modifier,
        contentPadding = contentPadding
    )
}
