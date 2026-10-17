package com.segundoaire.presentation.blocks

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segundoaire.presentation.designsystem.theme.Motion
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.feedback.rememberFeedback

/**
 * Bloques con su ViewModel. `collectAsStateWithLifecycle` deja de observar con
 * la pantalla detenida, y con eso se detiene también el tick del restante.
 * Mantener el botón inicia el bloque y abre el modo enfoque ([onOpenFocus]).
 */
@Composable
fun TimerRoute(
    onOpenFocus: () -> Unit,
    onEditApps: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: TimerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val feedback = rememberFeedback()

    TimerScreen(
        state = state,
        onDurationSelected = {
            feedback.select()
            viewModel.onDurationSelected(it)
        },
        onRigorSelected = {
            feedback.select()
            viewModel.onRigorSelected(it)
        },
        onStart = {
            feedback.startBlock()
            viewModel.onStart()
            onOpenFocus()
        },
        onOpenFocus = onOpenFocus,
        onToggleApp = { pkg, blocked ->
            feedback.select()
            viewModel.onToggleApp(pkg, blocked)
        },
        onEditApps = onEditApps,
        modifier = modifier,
        contentPadding = contentPadding
    )
}

/**
 * Capa del modo enfoque sobre el contenedor. Usa el mismo [TimerViewModel] que la
 * pestaña Bloques (el de la entrada `home`) y observa el bloque siempre, así un bloque
 * que vence con la app abierta se completa y se celebra aunque Bloques no esté a la vista.
 *
 * Se ve si [open] y hay un bloque en curso o recién completado. Entra subiendo 40 dp
 * (`Motion.rise(600)`) mientras se funde (`Motion.fade(500)`); el botón atrás minimiza.
 */
@Composable
fun FocusLayer(
    open: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onBackToPanel: () -> Unit,
    onEnded: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val motion = SegundoAireTheme.motion
    val feedback = rememberFeedback()
    val completed = state.completedMinutes != null
    val visible = open && (state.isRunning || completed)
    val risePx = with(LocalDensity.current) { FocusRise.roundToPx() }

    // Un bloque que termina con la app abierta se celebra en el modo enfoque
    LaunchedEffect(state.completedMinutes) {
        if (state.completedMinutes != null) {
            feedback.bloom()
            onOpen()
        }
    }
    val backToPanel = {
        viewModel.onCompletedSeen()
        onBackToPanel()
    }
    BackHandler(enabled = visible) { if (completed) backToPanel() else onClose() }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(motion.fade(Motion.FocusFadeMillis)) +
            slideInVertically(motion.rise(Motion.FocusRiseMillis)) { if (motion.reduced) 0 else risePx },
        exit = fadeOut(motion.fade(Motion.FocusFadeMillis)) +
            slideOutVertically(motion.rise(Motion.FocusRiseMillis)) { if (motion.reduced) 0 else risePx }
    ) {
        FocusMode(
            state = state,
            onEnd = {
                viewModel.onEnd()
                onClose()
                onEnded()
            },
            onBackToPanel = backToPanel,
            onMinimize = onClose
        )
    }
}

private val FocusRise = 40.dp
