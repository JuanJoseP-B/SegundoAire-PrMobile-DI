package com.segundoaire.presentation.onboarding

import android.Manifest
import android.os.Build
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segundoaire.R
import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.presentation.auth.AccountViewModel
import com.segundoaire.presentation.auth.EmailAuthField
import com.segundoaire.presentation.auth.EmailAuthViewModel
import com.segundoaire.presentation.designsystem.theme.GlassBackdrop
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glassBackdrop
import com.segundoaire.presentation.onboarding.sound.OnboardingFeedbackViewModel
import com.segundoaire.presentation.onboarding.sound.Sfx
import com.segundoaire.presentation.onboarding.sound.rememberHaptics
import com.segundoaire.presentation.onboarding.steps.BreathContent
import com.segundoaire.presentation.onboarding.steps.BreathText
import com.segundoaire.presentation.onboarding.steps.CommitContent
import com.segundoaire.presentation.onboarding.steps.CommitText
import com.segundoaire.presentation.onboarding.steps.CurtainOverlay
import com.segundoaire.presentation.onboarding.steps.CurtainState
import com.segundoaire.presentation.onboarding.steps.GoalsContent
import com.segundoaire.presentation.onboarding.steps.GoalsText
import com.segundoaire.presentation.onboarding.steps.HoldController
import com.segundoaire.presentation.onboarding.steps.LoginContent
import com.segundoaire.presentation.onboarding.steps.LoginPhase
import com.segundoaire.presentation.onboarding.steps.MomentContent
import com.segundoaire.presentation.onboarding.steps.MomentText
import com.segundoaire.presentation.onboarding.steps.NameContent
import com.segundoaire.presentation.onboarding.steps.NameText
import com.segundoaire.presentation.onboarding.steps.OnboardingPermission
import com.segundoaire.presentation.onboarding.steps.PermissionsContent
import com.segundoaire.presentation.onboarding.steps.PermissionsText
import com.segundoaire.presentation.onboarding.steps.PlanContent
import com.segundoaire.presentation.onboarding.steps.PlanText
import com.segundoaire.presentation.onboarding.steps.RevealContent
import com.segundoaire.presentation.onboarding.steps.RevealText
import com.segundoaire.presentation.onboarding.steps.UsageContent
import com.segundoaire.presentation.onboarding.steps.UsageText
import com.segundoaire.presentation.onboarding.steps.WelcomeContent
import com.segundoaire.presentation.onboarding.steps.WelcomeTexts
import com.segundoaire.presentation.onboarding.steps.planRows
import com.segundoaire.presentation.onboarding.tree.FallingLeaves
import com.segundoaire.presentation.onboarding.tree.GrowthVine
import com.segundoaire.presentation.onboarding.tree.OnboardingHill
import com.segundoaire.presentation.onboarding.tree.OnboardingTree
import com.segundoaire.presentation.onboarding.tree.SceneAtmosphere
import com.segundoaire.presentation.onboarding.tree.TreeInput
import com.segundoaire.presentation.onboarding.tree.TreeRules
import com.segundoaire.presentation.onboarding.tree.WelcomeDots
import com.segundoaire.presentation.onboarding.tree.rememberTreeAnimations
import com.segundoaire.presentation.setup.PermissionsViewModel
import com.segundoaire.presentation.setup.openAccessibilitySettings
import com.segundoaire.presentation.setup.openOverlaySettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.max

const val OnboardingGraphRoute = "onboarding"

/** Entrada para quien ya completó el onboarding pero le falta la cuenta: abre directo en el login. */
const val OnboardingAccountRoute = "onboarding/account"

private const val WelcomeFadeMillis = 420L

/** Lo que solo es animación del flujo: no se guarda ni sobrevive a girar la pantalla. */
@Stable
internal class OnboardingTransient {
    var gust by mutableIntStateOf(0)
    var shown by mutableStateOf(false)
    var petals by mutableStateOf(false)
    var committed by mutableStateOf(false)
    var breathDone by mutableStateOf(false)
    var shakeIndex by mutableIntStateOf(-1)
    var loginPhase by mutableStateOf(LoginPhase.Idle)
    var curtain by mutableStateOf(CurtainState.Up)
    val hold = HoldController()
}

/**
 * Onboarding "Lo que crece cuando pausas": un solo flujo de 14 pasos con el árbol que crece
 * con cada respuesta. El paso vive en [OnboardingViewModel]; atrás del sistema retrocede un
 * paso. [onFinished] se llama al cerrar el último paso, con el perfil y la cuenta ya guardados.
 */
@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    startStep: Int = OnboardingStep.Seed,
    viewModel: OnboardingViewModel = hiltViewModel(),
    permissionsViewModel: PermissionsViewModel = hiltViewModel(),
    emailViewModel: EmailAuthViewModel = hiltViewModel(),
    accountViewModel: AccountViewModel = hiltViewModel(),
    feedbackViewModel: OnboardingFeedbackViewModel = hiltViewModel()
) {
    remember(viewModel) { viewModel.ensureStartStep(startStep) }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionsState by permissionsViewModel.uiState.collectAsStateWithLifecycle()
    val emailState by emailViewModel.uiState.collectAsStateWithLifecycle()
    val accountState by accountViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val sound = feedbackViewModel.sound
    val haptics = rememberHaptics()
    val ui = remember { OnboardingTransient() }
    val step = state.step

    SideEffect { sound.enabled = state.soundEnabled }

    val go: (Int) -> Unit = { to ->
        if (to in OnboardingStep.Seed..OnboardingStep.Last && to != step) {
            sound.play(Sfx.Step)
            haptics.contextClick()
            viewModel.onStepChange(to)
        }
    }

    // Efectos al cambiar de paso: hojas que caen, cascadas, telón y temporizadores del paso
    val firstRun = remember { booleanArrayOf(true) }
    LaunchedEffect(step) {
        val restored = firstRun[0]
        firstRun[0] = false
        if (!restored) ui.gust++
        ui.shown = false
        ui.petals = false
        if (step < OnboardingStep.Login) ui.curtain = CurtainState.Up
        when (step) {
            OnboardingStep.Goals, OnboardingStep.Moment -> {
                delay(WelcomeFadeMillis)
                ui.shown = true
            }
            OnboardingStep.Commit -> {
                ui.hold.reset()
                ui.committed = false
            }
            OnboardingStep.Breath -> {
                ui.breathDone = false
                delay(OnboardingRules.BreathDoneMillis)
                ui.breathDone = true
            }
            OnboardingStep.Login -> {
                ui.loginPhase = LoginPhase.Idle
                if (restored) {
                    ui.curtain = CurtainState.Rest
                } else {
                    ui.curtain = CurtainState.Cover
                    delay(LoginCurtainRestMillis)
                    ui.curtain = CurtainState.Rest
                }
            }
            OnboardingStep.Done -> ui.curtain = CurtainState.Cover
        }
    }

    // Compromiso: al completarlo florece todo y, a los 1,6 s, se guarda y avanza
    LaunchedEffect(ui.committed) {
        if (!ui.committed || step != OnboardingStep.Commit) return@LaunchedEffect
        delay(OnboardingRules.CommitAdvanceMillis)
        if (viewModel.commit()) {
            go(OnboardingStep.Permissions)
        } else {
            // DataStore no pudo escribir: el compromiso vuelve a quedar disponible
            ui.committed = false
            ui.petals = false
            ui.hold.reset()
        }
    }

    // Permisos: los dos requeridos salen del sistema; las notificaciones, de su propio ajuste
    var notificationsOn by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        permissionsViewModel.refresh()
        notificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    val granted = mapOf(
        OnboardingPermission.Accessibility to permissionsState.isAccessibilityGranted,
        OnboardingPermission.Overlay to permissionsState.isOverlayGranted,
        OnboardingPermission.Notifications to notificationsOn
    )
    val grantedCount = granted.values.count { it }
    var lastGrantedCount by remember { mutableIntStateOf(grantedCount) }
    LaunchedEffect(grantedCount) {
        if (grantedCount > lastGrantedCount && step == OnboardingStep.Permissions) {
            sound.play(Sfx.Pop)
            haptics.confirm()
        }
        lastGrantedCount = grantedCount
    }

    // Login: el botón queda ocupado al menos 1,5 s y pasa al cierre cuando la cuenta se guardó
    LaunchedEffect(ui.loginPhase) {
        if (ui.loginPhase != LoginPhase.Busy) return@LaunchedEffect
        val began = SystemClock.elapsedRealtime()
        val completed = withTimeoutOrNull(OnboardingRules.LoginTimeoutMillis) {
            snapshotFlow { emailState.isCompleted || accountState.isCompleted }.first { it }
        }
        if (completed == null) {
            ui.loginPhase = LoginPhase.Idle
            return@LaunchedEffect
        }
        delay(max(0L, OnboardingRules.LoginBusyMillis - (SystemClock.elapsedRealtime() - began)))
        go(OnboardingStep.Done)
        sound.play(Sfx.Commit)
        haptics.confirm()
    }

    BackHandler(enabled = step > OnboardingStep.Seed) {
        // En el cierre y con el compromiso hecho no hay vuelta atrás
        val locked = step == OnboardingStep.Done || (step == OnboardingStep.Commit && ui.committed)
        if (!locked) go(step - 1)
    }

    val picks = state.goals.toPickIndexes()
    val moment = momentIndexOf(state.riskMoments)

    OnboardingScene(
        state = state,
        ui = ui,
        picks = picks,
        moment = moment,
        permissions = granted,
        emailState = emailState,
        onBack = { go(step - 1) },
        onNext = { go(step + 1) },
        onSoundToggle = viewModel::onSoundToggle,
        onNameChange = { value ->
            if (value.length > state.name.length) haptics.textHandleMove()
            viewModel.onNameChange(value)
        },
        onPickGoal = { index ->
            when (val result = OnboardingRules.togglePick(picks, index)) {
                is PickResult.Added -> {
                    viewModel.onGoalsChange(result.picks.map { GoalOptions[it].goal })
                    sound.play(Sfx.Bloom)
                    haptics.confirm()
                }
                is PickResult.Removed -> {
                    viewModel.onGoalsChange(result.picks.map { GoalOptions[it].goal })
                    sound.play(Sfx.Drop)
                    haptics.textHandleMove()
                }
                PickResult.Denied -> {
                    ui.shakeIndex = index
                    sound.play(Sfx.Deny)
                    haptics.reject()
                    scope.launch {
                        delay(ShakeResetMillis)
                        ui.shakeIndex = -1
                    }
                }
            }
        },
        onPickUsage = { index ->
            viewModel.onUsageRangeChange(usageRangeAt(index))
            sound.play(Sfx.Drop)
            haptics.textHandleMove()
        },
        onPickMoment = { index ->
            viewModel.onRiskMomentsChange(setOf(MomentOptions[index].moment))
            sound.play(Sfx.Pop)
            haptics.contextClick()
        },
        onHoldMark = {
            sound.play(Sfx.Drop)
            haptics.textHandleMove()
        },
        onCommit = {
            if (!ui.committed) {
                ui.committed = true
                ui.petals = true
                ui.gust++
                haptics.commit()
                sound.play(Sfx.Commit)
            }
        },
        onEnablePermission = { permission ->
            when (permission) {
                OnboardingPermission.Accessibility -> context.openAccessibilitySettings()
                OnboardingPermission.Overlay -> context.openOverlaySettings()
                OnboardingPermission.Notifications ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        notificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()
                    }
            }
        },
        onEmailChange = emailViewModel::onEmailChange,
        onPasswordChange = emailViewModel::onPasswordChange,
        onFieldBlur = emailViewModel::onFieldBlur,
        onCreateAccount = {
            if (ui.loginPhase == LoginPhase.Idle) {
                if (emailState.canSubmit) {
                    ui.loginPhase = LoginPhase.Busy
                    haptics.contextClick()
                    emailViewModel.onSubmit()
                } else {
                    // Faltan datos: muestra los errores de los dos campos sin esperar a que los toque
                    emailViewModel.onFieldBlur(EmailAuthField.EMAIL)
                    emailViewModel.onFieldBlur(EmailAuthField.PASSWORD)
                    haptics.reject()
                }
            }
        },
        onProvider = { mode ->
            if (ui.loginPhase == LoginPhase.Idle) {
                ui.loginPhase = LoginPhase.Busy
                haptics.contextClick()
                accountViewModel.onAuthModeSelected(mode)
            }
        },
        onFinished = onFinished
    )
}

private const val LoginCurtainRestMillis = 850L
private const val ShakeResetMillis = 400L

@Composable
private fun OnboardingScene(
    state: OnboardingUiState,
    ui: OnboardingTransient,
    picks: List<Int>,
    moment: Int,
    permissions: Map<OnboardingPermission, Boolean>,
    emailState: com.segundoaire.presentation.auth.EmailAuthUiState,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSoundToggle: () -> Unit,
    onNameChange: (String) -> Unit,
    onPickGoal: (Int) -> Unit,
    onPickUsage: (Int) -> Unit,
    onPickMoment: (Int) -> Unit,
    onHoldMark: () -> Unit,
    onCommit: () -> Unit,
    onEnablePermission: (OnboardingPermission) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onFieldBlur: (EmailAuthField) -> Unit,
    onCreateAccount: () -> Unit,
    onProvider: (AuthMode) -> Unit,
    onFinished: () -> Unit
) {
    val motion = SegundoAireTheme.motion
    val step = state.step
    val layer = TreeRules.layerIndex(step)
    val layerPosition = animateFloatAsState(layer.toFloat(), motion.layer(), label = "layerPosition")

    val treeInput = TreeInput(
        step = step,
        nameLength = state.name.length,
        picks = picks.toSet(),
        usageIndex = state.usageRange.index,
        committed = ui.committed
    )
    val animations = rememberTreeAnimations(treeInput, motion)

    val welcomePage = step.coerceAtMost(OnboardingStep.WelcomeLast)
    val number = OnboardingStep.number(step)
    val total = OnboardingStep.QuestionCount
    val goalLabels = picks.map { stringResource(GoalOptions[it].label) }
    val momentBlock = if (moment >= 0) {
        stringResource(MomentOptions[moment].block)
    } else {
        stringResource(MomentOptions[2].block)
    }
    val plan = planRows(goalLabels, momentBlock, state.suggestedRigor == RigorLevel.STRICT)

    OnboardingFrame {
        GlassBackdrop {
            Box(Modifier.fillMaxSize()) {
                // Fondo vivo: es lo que desenfocan los vidrios. Cielo y colina cubren toda la
                // ventana; el árbol y las hojas van en la columna de diseño
                Box(Modifier.fillMaxSize().glassBackdrop()) {
                    SceneAtmosphere(
                        moment = moment,
                        skyVisible = step == OnboardingStep.Moment && moment >= 0,
                        firefliesVisible = step == OnboardingStep.Moment && (moment == 2 || moment == 3),
                        reducedMotion = motion.reduced
                    )
                    OnboardingHill(animations)
                    FrameColumn {
                        OnboardingTree(
                            animations,
                            rootProgress = { index -> TreeRules.rootProgress(step, ui.hold.hold, index) }
                        )
                        FallingLeaves(gust = ui.gust, petals = ui.petals, reducedMotion = motion.reduced)
                    }
                }

                FrameColumn {
                    WelcomeDots(page = welcomePage, visible = step <= OnboardingStep.WelcomeLast)
                    GrowthVine(step)

                    // Bienvenida: textos a media velocidad y botón
                    StepLayer(0, layer, layerPosition, 0.5f) { WelcomeTexts(welcomePage) }
                    StepLayer(0, layer, layerPosition, 1f) { WelcomeContent(welcomePage, onNext) }

                    StepLayer(1, layer, layerPosition, 0.5f) { NameText(number, total) }
                    StepLayer(1, layer, layerPosition, 1f) {
                        NameContent(
                            name = state.name,
                            nameError = state.nameError,
                            canContinue = state.isNameValid,
                            onNameChange = onNameChange,
                            onContinue = onNext
                        )
                    }

                    StepLayer(2, layer, layerPosition, 0.5f) { GoalsText(number, total, state.displayName) }
                    StepLayer(2, layer, layerPosition, 1f) {
                        GoalsContent(
                            picks = picks,
                            shown = ui.shown && step == OnboardingStep.Goals,
                            shakeIndex = ui.shakeIndex,
                            onPick = onPickGoal,
                            onContinue = onNext
                        )
                    }

                    StepLayer(3, layer, layerPosition, 0.5f) { UsageText(number, total) }
                    StepLayer(3, layer, layerPosition, 1f) {
                        UsageContent(use = state.usageRange.index, onPick = onPickUsage, onContinue = onNext)
                    }

                    StepLayer(4, layer, layerPosition, 0.5f) { MomentText(number, total) }
                    StepLayer(4, layer, layerPosition, 1f) {
                        MomentContent(
                            moment = moment,
                            shown = ui.shown && step == OnboardingStep.Moment,
                            onPick = onPickMoment,
                            onContinue = onNext
                        )
                    }

                    StepLayer(5, layer, layerPosition, 0.5f) { RevealText(number, total) }
                    StepLayer(5, layer, layerPosition, 1f) {
                        RevealContent(
                            active = step == OnboardingStep.Reveal,
                            usageIndex = state.usageRange.index,
                            onContinue = onNext
                        )
                    }

                    StepLayer(6, layer, layerPosition, 0.5f) { BreathText(number, total) }
                    StepLayer(6, layer, layerPosition, 1f) {
                        BreathContent(active = step == OnboardingStep.Breath, done = ui.breathDone, onContinue = onNext)
                    }

                    StepLayer(7, layer, layerPosition, 0.5f) { PlanText(number, total, state.displayName) }
                    StepLayer(7, layer, layerPosition, 1f) {
                        PlanContent(active = step == OnboardingStep.Plan, rows = plan, onContinue = onNext)
                    }

                    StepLayer(8, layer, layerPosition, 0.5f) { CommitText(number, total) }
                    StepLayer(8, layer, layerPosition, 1f) {
                        CommitContent(
                            active = step == OnboardingStep.Commit,
                            name = state.displayName,
                            committed = ui.committed,
                            controller = ui.hold,
                            onMark = onHoldMark,
                            onCommit = onCommit
                        )
                    }

                    StepLayer(9, layer, layerPosition, 0.5f) { PermissionsText(number, total) }
                    StepLayer(9, layer, layerPosition, 1f) {
                        PermissionsContent(granted = permissions, onEnable = onEnablePermission, onContinue = onNext)
                    }

                    StepLayer(10, layer, layerPosition, 1f) {
                        LoginContent(
                            state = emailState,
                            phase = ui.loginPhase,
                            onEmailChange = onEmailChange,
                            onPasswordChange = onPasswordChange,
                            onFieldBlur = onFieldBlur,
                            onCreate = onCreateAccount,
                            onWeb = { onProvider(AuthMode.GOOGLE) },
                            onDevice = { onProvider(AuthMode.APPLE) },
                            onMail = onCreateAccount,
                            onGuest = { onProvider(AuthMode.GUEST) }
                        )
                    }
                }

                CurtainOverlay(
                    state = ui.curtain,
                    done = step == OnboardingStep.Done,
                    name = state.displayName,
                    onFinish = onFinished
                )

                // Arriba del telón: atrás y sonido
                FrameColumn {
                    val onCurtain = step >= OnboardingStep.Login
                    BackButton(
                        visible = step > OnboardingStep.Seed && step < OnboardingStep.Done &&
                            !(step == OnboardingStep.Commit && ui.committed),
                        onCurtain = onCurtain,
                        onClick = onBack
                    )
                    SoundButton(enabled = state.soundEnabled, onCurtain = onCurtain, onToggle = onSoundToggle)
                }
            }
        }
    }
}

/**
 * Capa de un paso: se desplaza según su distancia al paso actual (los textos a media velocidad,
 * para el efecto de profundidad) y se funde. Solo existe cuando el paso actual está a una capa o
 * menos; la que no está al frente queda fuera de la accesibilidad.
 */
@Composable
private fun StepLayer(
    index: Int,
    current: Int,
    position: State<Float>,
    factor: Float,
    content: @Composable BoxScope.() -> Unit
) {
    if (abs(index - current) > 1) return
    val motion = SegundoAireTheme.motion
    val on = index == current
    val alpha = animateFloatAsState(
        targetValue = if (on) 1f else 0f,
        animationSpec = motion.layerFade(on),
        label = "layerAlpha"
    )
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = (index - position.value) * TreeRules.FrameWidth * factor * density
                this.alpha = alpha.value
            }
            .then(if (on) Modifier else Modifier.clearAndSetSemantics { }),
        content = content
    )
}

@Composable
private fun BackButton(visible: Boolean, onCurtain: Boolean, onClick: () -> Unit) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val alpha = animateFloatAsState(if (visible) 1f else 0f, motion.fade(300), label = "backAlpha")
    val tint = animateColorAsState(
        if (onCurtain) colors.onSecondary else colors.onBackground,
        motion.fade(400),
        label = "backTint"
    )
    val label = stringResource(R.string.onboarding_back)
    Box(
        Modifier
            .offset(x = 8.dp, y = 12.dp)
            .size(48.dp)
            .graphicsLayer { this.alpha = alpha.value }
            .clickable(
                enabled = visible,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .semantics { contentDescription = label },
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        SvgIcon(OnboardingIcons.Back, tint.value, size = 24.dp)
    }
}

@Composable
private fun SoundButton(enabled: Boolean, onCurtain: Boolean, onToggle: () -> Unit) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val tint = animateColorAsState(
        if (onCurtain) colors.onSecondary else colors.onBackground,
        motion.fade(400),
        label = "soundTint"
    )
    val label = stringResource(R.string.onb_sound)
    Box(
        Modifier
            .offset(x = TreeRules.FrameWidth.dp - 56.dp, y = 12.dp)
            .size(48.dp)
            .toggleable(
                value = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Switch,
                onValueChange = { onToggle() }
            )
            .semantics {
                contentDescription = label
            },
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        SvgIcon(OnboardingIcons.SoundBase, tint.value, size = 22.dp)
        SvgIcon(
            path = if (enabled) OnboardingIcons.SoundOn else OnboardingIcons.SoundOff,
            color = tint.value,
            size = 22.dp
        )
    }
}
