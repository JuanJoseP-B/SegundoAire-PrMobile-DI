package com.segundoaire.presentation.profile

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Perfil con sus ViewModels. Al volver a la app (`ON_RESUME`) se relee el idioma,
 * por si se cambió desde los ajustes de idioma por app del sistema.
 */
@Composable
fun ProfileRoute(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    onOpenTutorial: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by settingsViewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { settingsViewModel.onResume() }

    val currentOpenTutorial by rememberUpdatedState(onOpenTutorial)
    val settingsActions = remember(settingsViewModel) {
        SettingsActions(
            onLanguageSelected = settingsViewModel::onLanguageSelected,
            onThemeModeSelected = settingsViewModel::onThemeModeSelected,
            onOpenTutorial = { currentOpenTutorial() }
        )
    }

    ProfileScreen(
        state = state,
        settings = settings,
        onNameChange = viewModel::onNameChange,
        onGoalsChange = viewModel::onGoalsChange,
        onSave = viewModel::onSave,
        settingsActions = settingsActions,
        modifier = modifier,
        contentPadding = contentPadding
    )
}
