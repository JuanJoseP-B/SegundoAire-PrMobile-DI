package com.segundoaire.presentation.panel

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segundoaire.R
import com.segundoaire.presentation.feedback.rememberFeedback
import com.segundoaire.presentation.main.MainTab
import com.segundoaire.presentation.setup.openAccessibilitySettings
import com.segundoaire.presentation.setup.openOverlaySettings

/**
 * Panel con su ViewModel. Al volver de Ajustes o de otra app (`ON_RESUME`) relee
 * permisos, hora y día. Las acciones del coach que salen del Panel van al contenedor:
 * [onOpenTab] cambia de pestaña, [onOpenActiveBlock] abre el bloque en curso y
 * [onToast] muestra un aviso breve.
 *
 * [onTitleLongClick] solo se pasa en debug, para abrir el catálogo de componentes.
 */
@Composable
fun PanelRoute(
    onOpenTab: (MainTab) -> Unit,
    onOpenActiveBlock: () -> Unit,
    onToast: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    onTitleLongClick: (() -> Unit)? = null,
    viewModel: PanelViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val remindText = stringResource(R.string.coach_toast_remind)
    val openTab by rememberUpdatedState(onOpenTab)
    val openBlock by rememberUpdatedState(onOpenActiveBlock)
    val toast by rememberUpdatedState(onToast)
    val feedback = rememberFeedback()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PanelEffect.OpenBlocks -> openTab(MainTab.BLOCKS)
                PanelEffect.OpenProgress -> openTab(MainTab.PROGRESS)
                PanelEffect.OpenActiveBlock -> openBlock()
                PanelEffect.RemindToast -> toast(remindText)
            }
        }
    }

    PanelScreen(
        state = state,
        onFixPermission = {
            // Abre el ajuste del permiso que falta, no la lista general
            when (state.missingPermission) {
                MissingPermission.ACCESSIBILITY -> context.openAccessibilitySettings()
                MissingPermission.OVERLAY -> context.openOverlaySettings()
                null -> Unit
            }
        },
        onLeavesShown = viewModel::onLeavesShown,
        onActiveBlockClick = viewModel::onActiveBlockClick,
        onCoachNext = {
            feedback.select()
            viewModel.onCoachNext()
        },
        onCoachAsk = {
            feedback.select()
            viewModel.onCoachAsk(it)
        },
        onCoachAction = {
            feedback.select()
            viewModel.onCoachAction(it)
        },
        modifier = modifier,
        contentPadding = contentPadding,
        onTitleLongClick = onTitleLongClick
    )
}
