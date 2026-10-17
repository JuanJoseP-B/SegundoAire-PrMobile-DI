package com.segundoaire.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segundoaire.R
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.presentation.designsystem.components.PrimaryButton
import com.segundoaire.presentation.designsystem.components.Rigor
import com.segundoaire.presentation.designsystem.components.RigorSelector
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.feedback.rememberFeedback
import com.segundoaire.presentation.panel.MissingPermission
import com.segundoaire.presentation.setup.openAccessibilitySettings
import com.segundoaire.presentation.setup.openOverlaySettings
import kotlinx.coroutines.launch

/**
 * Perfil con su ViewModel. Al volver de Ajustes (`ON_RESUME`) relee los permisos.
 * "Editar" abre la hoja del nombre y las metas, que se cierra al guardar; "Plan y modo"
 * abre su hoja; "Permisos" lleva al ajuste del que falta y "Cuenta" a [onOpenAccount].
 * Si la imagen para compartir no se puede preparar, se avisa con [onToast].
 */
@Composable
fun ProfileRoute(
    onOpenAccount: () -> Unit,
    onToast: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = SegundoAireTheme.colors
    val chooserTitle = stringResource(R.string.profile_share_chooser)
    val shareText = stringResource(R.string.profile_share_text)
    val shareFailed = stringResource(R.string.profile_share_failed)
    val toast by rememberUpdatedState(onToast)
    val feedback = rememberFeedback()
    var editing by rememberSaveable { mutableStateOf(false) }
    var planOpen by rememberSaveable { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

    // Guardar con éxito cierra la hoja; si falla, se queda abierta con el aviso
    LaunchedEffect(state.feedback) {
        if (state.feedback == SaveFeedback.SAVED) editing = false
    }

    ProfileScreen(
        state = state,
        onEditGoals = {
            viewModel.onEditStart()
            editing = true
        },
        onOpenPlan = { planOpen = true },
        onSoundChange = {
            feedback.select()
            viewModel.onSoundChange(it)
        },
        onOpenPermissions = {
            when (state.missingPermission) {
                MissingPermission.OVERLAY -> context.openOverlaySettings()
                // Con todo concedido se abre igual el de accesibilidad, para revisarlo
                MissingPermission.ACCESSIBILITY, null -> context.openAccessibilitySettings()
            }
        },
        onOpenAccount = onOpenAccount,
        onShare = {
            scope.launch {
                val file = ShareTree.render(context, state.tree, state.rings, colors)
                if (file != null) ShareTree.share(context, file, chooserTitle, shareText) else toast(shareFailed)
            }
        },
        modifier = modifier,
        contentPadding = contentPadding
    )

    if (editing) {
        ProfileEditSheet(
            state = state,
            onNameChange = viewModel::onNameChange,
            onGoalsChange = viewModel::onGoalsChange,
            onSave = viewModel::onSave,
            onDismiss = { editing = false }
        )
    }
    if (planOpen) {
        PlanSheet(rigor = state.rigor, onRigorChange = viewModel::onRigorChange, onDismiss = { planOpen = false })
    }
}

/** "Plan y modo": el rigor con el que arrancan los bloques nuevos. Se guarda al elegir. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanSheet(rigor: RigorLevel, onRigorChange: (RigorLevel) -> Unit, onDismiss: () -> Unit) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.background
    ) {
        Column(
            Modifier.padding(start = spacing.space5, end = spacing.space5, bottom = spacing.space6),
            verticalArrangement = Arrangement.spacedBy(spacing.space4)
        ) {
            Text(
                text = stringResource(R.string.profile_row_plan),
                style = SegundoAireTheme.typography.serif(size = 26, lineHeight = 32),
                color = colors.onBackground,
                modifier = Modifier.semantics { heading() }
            )
            Text(text = stringResource(R.string.profile_plan_body), style = SegundoAireTheme.typography.bodyMd, color = colors.onBackgroundMuted)
            RigorSelector(
                selected = if (rigor == RigorLevel.STRICT) Rigor.Strict else Rigor.Mindful,
                onSelected = { onRigorChange(if (it == Rigor.Strict) RigorLevel.STRICT else RigorLevel.MINDFUL) },
                modifier = Modifier.fillMaxWidth()
            )
            PrimaryButton(text = stringResource(R.string.profile_plan_done), onClick = onDismiss, modifier = Modifier.fillMaxWidth())
        }
    }
}

/**
 * La subpantalla "Cuenta" con su ViewModel. Al volver a la app (`ON_RESUME`) se relee el
 * idioma, por si se cambió desde los ajustes de idioma por app del sistema.
 */
@Composable
fun AccountSettingsRoute(
    onBack: () -> Unit,
    onOpenTutorial: () -> Unit,
    onResetCompleted: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

    val currentOpenTutorial by rememberUpdatedState(onOpenTutorial)
    val currentResetCompleted by rememberUpdatedState(onResetCompleted)

    // Con el estado y no con un evento: si el idioma recrea la actividad a mitad del
    // reinicio, la actividad nueva sigue viendo `isResetCompleted` y navega igual
    LaunchedEffect(settings.isResetCompleted) {
        if (settings.isResetCompleted) currentResetCompleted()
    }
    val actions = remember(viewModel) {
        SettingsActions(
            onLanguageSelected = viewModel::onLanguageSelected,
            onThemeModeSelected = viewModel::onThemeModeSelected,
            onOpenTutorial = { currentOpenTutorial() },
            onDemoModeChange = viewModel::onDemoModeChange,
            onResetConfirmed = viewModel::onResetConfirmed
        )
    }

    AccountSettingsScreen(settings = settings, actions = actions, onBack = onBack)
}
