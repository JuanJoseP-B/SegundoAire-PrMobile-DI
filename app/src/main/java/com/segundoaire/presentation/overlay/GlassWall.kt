package com.segundoaire.presentation.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segundoaire.R
import com.segundoaire.domain.model.GlassWallState
import com.segundoaire.domain.model.InterceptionIntent
import com.segundoaire.presentation.designsystem.components.focusRing
import com.segundoaire.presentation.designsystem.components.pressScale
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.Motion
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.feedback.rememberFeedback
import com.segundoaire.presentation.onboarding.emphasized
import com.segundoaire.presentation.tree.TreeGeometry
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Momento del GlassWall dentro de su ventana. */
enum class GlassWallPhase {
    /** Recién agregado a la ventana: todavía no se ve nada. */
    Waiting,

    /** Entra y corre la coreografía. */
    Visible,

    /** Sale todo a la vez; al terminar avisa para quitar la ventana. */
    Leaving
}

/**
 * Lo que muestra el GlassWall: [state] decide las acciones; [appName] y [minutesToday]
 * la pregunta; [weekLeaves] las hojas del árbol esta semana; [cooldownMinutes] lo que
 * dura "Continuar" sin bloque.
 */
@Immutable
data class GlassWallContent(
    val state: GlassWallState = GlassWallState.NoBlock,
    val appName: String = "",
    val minutesToday: Int = 0,
    val weekLeaves: Int = 0,
    val cooldownMinutes: Int = 5
)

// Medidas de la referencia (marco de 390 × 844)
private val BreathAreaHeight = 300.dp
private const val AreaWidth = 390f
private val CanopyPivot = Offset(195f, 172f)
private const val TrunkPath = "M195 300 C192 270 198 250 195 225"
private val LeafOrigin = Offset(178f, 130f)
private const val LeafSize = 34f
private const val LeafViewBox = 24f
private const val StemPath = "M2 14 C8 14 12 10 14 6"
private const val StemLeafPath = "M14 6 C16 2 22 1 26 3 C24 8 18 9 14 6Z"
private val WallBlobs = listOf(
    floatArrayOf(195f, 150f, 70f), floatArrayOf(140f, 170f, 50f), floatArrayOf(250f, 170f, 50f),
    floatArrayOf(165f, 115f, 45f), floatArrayOf(225f, 112f, 45f), floatArrayOf(195f, 195f, 55f),
    floatArrayOf(195f, 95f, 40f)
)
private val Intents = listOf(
    InterceptionIntent.REPLY_MESSAGE to R.string.wall_intent_reply,
    InterceptionIntent.SEARCH to R.string.wall_intent_search,
    InterceptionIntent.BORED to R.string.wall_intent_bored
)

/**
 * El GlassWall a pantalla completa, con la coreografía de la referencia:
 *
 * 1. 0 ms: sube el velo (y el blur de la ventana, que aplica quien la aloja con
 *    [onBackdropProgress]); a los 320 ms entra el contenido con fundido y escala de 0.92 a 1
 *    y una hoja cae al centro (1,6 s).
 * 2. 1,7 s: respiración de 8 s (copa de 0.62 a 1 y de vuelta, "Inhala / Exhala", sonido de
 *    calma). Sin botones.
 * 3. 9,7 s: la pregunta con los minutos de hoy y las hojas de la semana. Mindful (y sin
 *    bloque): chips de intención ([onIntent]) y "Continuar" tras 10 s, con un tallo que se
 *    dibuja y una hoja que brota. Strict: copa gris, "Se abre a las {hora}" y sin continuar.
 * 4. "Volver": la hoja sube y se va (1 s) y luego [onBack].
 * 5. "Continuar": todo se funde y luego [onContinue].
 *
 * Es un diálogo modal y el foco empieza en "Volver". Con reducir movimiento la respiración
 * es un cambio de opacidad y no hay hoja que cae ni que sube. [phase] lo maneja la ventana.
 */
@Composable
fun GlassWall(
    phase: GlassWallPhase,
    content: GlassWallContent,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onIntent: (InterceptionIntent) -> Unit,
    modifier: Modifier = Modifier,
    onBackdropProgress: (Float) -> Unit = {},
    onHidden: () -> Unit = {},
    nowMillis: () -> Long = System::currentTimeMillis
) {
    val colors = SegundoAireTheme.colors
    val motion by rememberUpdatedState(SegundoAireTheme.motion)
    val reduced = motion.reduced
    val feedback = rememberFeedback()
    val scope = rememberCoroutineScope()
    val backdrop = remember { Animatable(1f) }
    val shown = remember { Animatable(0f) }
    val fall = remember { Animatable(0f) }
    val breath = remember { Animatable(0f) }
    val rise = remember { Animatable(0f) }
    var step by remember { mutableStateOf(WallStep.Enter) }
    val notifyBackdrop by rememberUpdatedState(onBackdropProgress)
    val notifyHidden by rememberUpdatedState(onHidden)
    val back by rememberUpdatedState(onBack)
    val proceed by rememberUpdatedState(onContinue)

    LaunchedEffect(phase) {
        when (phase) {
            GlassWallPhase.Waiting -> Unit
            GlassWallPhase.Visible -> coroutineScope {
                // Otra intercepción mientras salía: la coreografía empieza de nuevo
                step = WallStep.Enter
                fall.snapTo(0f)
                breath.snapTo(0f)
                rise.snapTo(0f)
                // Fondo opaco desde el primer frame: la app bloqueada nunca se ve detrás
                backdrop.snapTo(1f)
                notifyBackdrop(1f)
                launch { shown.animateTo(1f, motion.fade(Motion.WallContentMillis, Motion.WallContentDelayMillis)) }
                if (!reduced) launch { fall.animateTo(1f, motion.gust(Motion.WallLeafFallMillis)) } else fall.snapTo(1f)
                launch {
                    delay(GlassWallChoreography.BreathStartMillis)
                    if (step != WallStep.Enter) return@launch
                    step = WallStep.Breath
                    feedback.calm()
                    breath.animateTo(1f, motion.gust(Motion.WallBreathMillis))
                }
                launch {
                    delay(GlassWallChoreography.AskAtMillis)
                    if (step == WallStep.Enter || step == WallStep.Breath) step = WallStep.Ask
                }
            }
            GlassWallPhase.Leaving -> {
                coroutineScope {
                    launch { shown.animateTo(0f, motion.calmExit()) }
                    launch { backdrop.animateTo(0f, motion.calmExit()) { notifyBackdrop(value) } }
                }
                notifyHidden()
            }
        }
    }

    val onBackClick: () -> Unit = {
        if (step == WallStep.Ask) {
            step = WallStep.Rise
            feedback.back()
            scope.launch {
                if (!reduced) rise.animateTo(1f, motion.gust(Motion.WallLeafRiseMillis))
                back()
            }
        }
    }
    val onContinueClick: () -> Unit = {
        if (step == WallStep.Ask) {
            step = WallStep.Leave
            scope.launch {
                coroutineScope {
                    launch { shown.animateTo(0f, motion.fade(Motion.WallLeaveMillis)) }
                    launch { backdrop.animateTo(0f, motion.fade(Motion.WallLeaveMillis)) { notifyBackdrop(value) } }
                }
                proceed()
            }
        }
    }

    val title = stringResource(R.string.overlay_title)
    // Fuera de la composición mientras no se ve: un contenido transparente seguiría recibiendo toques
    val contentVisible by remember { derivedStateOf { shown.value > 0f } }
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind { drawRect(colors.wallScrim, alpha = backdrop.value) }
            .semantics {
                dialog()
                paneTitle = title
            }
    ) {
        if (contentVisible || phase == GlassWallPhase.Visible) {
            WallContent(
                content = content,
                step = step,
                fall = { fall.value },
                breath = { breath.value },
                rise = { rise.value },
                reduced = reduced,
                onBack = onBackClick,
                onContinue = onContinueClick,
                onIntent = { intent ->
                    feedback.select()
                    onIntent(intent)
                },
                nowMillis = nowMillis,
                modifier = Modifier.graphicsLayer {
                    val scale = motion.calmEnterScale + (1f - motion.calmEnterScale) * shown.value
                    alpha = shown.value
                    scaleX = scale
                    scaleY = scale
                }
            )
        }
    }
}

@Composable
private fun WallContent(
    content: GlassWallContent,
    step: WallStep,
    fall: () -> Float,
    breath: () -> Float,
    rise: () -> Float,
    reduced: Boolean,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onIntent: (InterceptionIntent) -> Unit,
    nowMillis: () -> Long,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val spacing = SegundoAireTheme.spacing
    val state = content.state
    // Un bloque Strict que ya terminó se trata como sin bloque
    val strict = state is GlassWallState.Strict && nowMillis() < state.blockEndsAt
    val modeName = when {
        strict -> stringResource(R.string.rigor_strict)
        state is GlassWallState.Mindful -> stringResource(R.string.rigor_mindful)
        else -> null
    }

    Column(
        modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = spacing.space4),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(spacing.space6))
        Text(
            text = (if (modeName != null) stringResource(R.string.wall_eyebrow_mode, content.appName, modeName) else content.appName)
                .toUpperCase(Locale.current),
            style = typography.labelSmCaps.copy(fontWeight = FontWeight.W600),
            color = colors.wallMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(spacing.space2))
        Text(
            text = emphasized(stringResource(R.string.wall_title), typography.serifEmphasis.copy(color = colors.accent)),
            style = typography.serif(size = 32, lineHeight = 40, tracking = typography.serifTitle.letterSpacing),
            color = colors.wallOn,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.weight(1f))
        BreathArea(step = step, winter = strict, fall = fall, breath = breath, rise = rise, reduced = reduced)
        Spacer(Modifier.weight(1f))
        AskBlock(
            content = content,
            strict = strict,
            visible = step == WallStep.Ask || step == WallStep.Rise,
            onBack = onBack,
            onContinue = onContinue,
            onIntent = onIntent,
            modifier = Modifier.padding(bottom = spacing.space8)
        )
    }
}

/** Copa, tronco, la hoja y "Inhala / Exhala", con las coordenadas del área de 390 × 300. */
@Composable
private fun BreathArea(
    step: WallStep,
    winter: Boolean,
    fall: () -> Float,
    breath: () -> Float,
    rise: () -> Float,
    reduced: Boolean
) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val trunk = remember { PathParser().parsePathString(TrunkPath).toPath() }
    val leaf = remember { PathParser().parsePathString(TreeGeometry.FallingLeafPath).toPath() }
    val asking = step == WallStep.Ask || step == WallStep.Rise
    val restScale by animateFloatAsState(
        if (asking) GlassWallChoreography.CanopyAsk else GlassWallChoreography.CanopyRest,
        SegundoAireTheme.motion.fade(Motion.WallCanopyMillis),
        label = "canopy"
    )
    val breathing = step == WallStep.Breath
    val tones = listOf(colors.tree1, colors.tree2, colors.tree3)

    Box(Modifier.fillMaxWidth().height(BreathAreaHeight), contentAlignment = Alignment.TopCenter) {
        Canvas(Modifier.requiredSize(AreaWidth.dp, BreathAreaHeight).clearAndSetSemantics { }) {
            scale(density, Offset.Zero) {
                val breathScale = GlassWallChoreography.breathScale(breath())
                // Con reducir movimiento la copa no crece: respira con la opacidad
                val canopyScale = if (breathing && !reduced) breathScale else restScale
                val canopyAlpha = if (breathing && reduced) 0.6f + 0.4f * ((breathScale - 0.62f) / 0.38f) else 1f
                scale(canopyScale, CanopyPivot) {
                    WallBlobs.forEachIndexed { i, b ->
                        val color = if (winter) colors.wallWinter else tones[i % 3]
                        drawCircle(color.copy(alpha = color.alpha * canopyAlpha), b[2], Offset(b[0], b[1]))
                    }
                }
                drawPath(trunk, colors.wallTrunk, style = Stroke(7f, cap = StrokeCap.Round))
                val pose = when {
                    step == WallStep.Rise && !reduced -> GlassWallChoreography.leafRise(rise())
                    reduced -> GlassWallChoreography.LeafRest
                    fall() < 1f -> GlassWallChoreography.leafFall(fall())
                    else -> GlassWallChoreography.LeafRest
                }
                if (!(step == WallStep.Rise && reduced)) drawWallLeaf(leaf, pose, colors.wallLeaf, colors.wallOn)
            }
        }
        if (breathing) {
            Box(Modifier.align(Alignment.BottomCenter).height(40.dp)) {
                Text(
                    text = stringResource(R.string.wall_inhale),
                    style = typography.serif(size = 26, lineHeight = 32, weight = FontWeight.W400, italic = true),
                    color = colors.wallOn,
                    modifier = Modifier.graphicsLayer { alpha = GlassWallChoreography.inhaleAlpha(breath()) }
                )
                Text(
                    text = stringResource(R.string.wall_exhale),
                    style = typography.serif(size = 26, lineHeight = 32, weight = FontWeight.W400, italic = true),
                    color = colors.wallOn,
                    modifier = Modifier.graphicsLayer { alpha = GlassWallChoreography.exhaleAlpha(breath()) }
                )
            }
        }
    }
}

private fun DrawScope.drawWallLeaf(leaf: Path, pose: WallLeafPose, fill: Color, rib: Color) {
    if (pose.alpha <= 0f) return
    val half = LeafSize / 2f
    translate(LeafOrigin.x + pose.offset.x, LeafOrigin.y + pose.offset.y) {
        rotate(pose.rotation, Offset(half, half)) {
            scale(pose.scale, Offset(half, half)) {
                scale(LeafSize / LeafViewBox, Offset.Zero) {
                    drawPath(leaf, fill.copy(alpha = fill.alpha * pose.alpha))
                    drawLine(rib.copy(alpha = 0.4f * pose.alpha), Offset(2f, 12f), Offset(20f, 12f), strokeWidth = 1f)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AskBlock(
    content: GlassWallContent,
    strict: Boolean,
    visible: Boolean,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onIntent: (InterceptionIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val spacing = SegundoAireTheme.spacing
    val motion = SegundoAireTheme.motion
    val progress by animateFloatAsState(if (visible) 1f else 0f, motion.rise(Motion.WallAskMillis), label = "ask")
    val risePx = with(LocalDensity.current) { 16.dp.toPx() }
    val focus = remember { FocusRequester() }
    var intent by remember { mutableStateOf<InterceptionIntent?>(null) }
    val inBlock = content.state !is GlassWallState.NoBlock

    // El foco empieza en "Volver" en cuanto aparece la pregunta
    LaunchedEffect(visible) { if (visible) runCatching { focus.requestFocus() } }

    Column(
        modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = progress
                translationY = if (motion.reduced) 0f else risePx * (1f - progress)
            }
            // Sin botones durante la respiración: invisible no se toca ni se anuncia
            .then(if (visible) Modifier else Modifier.clearAndSetSemantics { }),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (content.minutesToday > 0) {
                stringResource(R.string.wall_minutes_today, content.minutesToday)
            } else {
                stringResource(R.string.wall_minutes_none)
            },
            style = typography.labelLg.copy(fontSize = 17.sp, lineHeight = 24.sp),
            color = colors.wallOn,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (content.weekLeaves > 0) {
                pluralStringResource(R.plurals.wall_week_leaves, content.weekLeaves, content.weekLeaves)
            } else {
                stringResource(R.string.wall_week_leaves_none)
            },
            style = typography.bodyMd.copy(fontSize = 13.sp),
            color = colors.wallMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
        if (strict && content.state is GlassWallState.Strict) {
            OpensAtChip(content.state.blockEndsAt, Modifier.padding(top = 14.dp))
        } else {
            Text(
                text = stringResource(R.string.wall_intent_question),
                style = typography.labelMd.copy(fontWeight = FontWeight.W400),
                color = colors.wallMuted,
                modifier = Modifier.padding(top = 14.dp)
            )
            FlowRow(
                modifier = Modifier.padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
            ) {
                Intents.forEach { (value, label) ->
                    IntentChip(
                        text = stringResource(label),
                        selected = intent == value,
                        enabled = visible,
                        onClick = {
                            intent = value
                            onIntent(value)
                        }
                    )
                }
            }
        }
        WallButton(
            text = stringResource(if (inBlock) R.string.overlay_back_to_block else R.string.overlay_exit),
            onClick = onBack,
            enabled = visible,
            modifier = Modifier
                .padding(top = spacing.space4)
                .focusRequester(focus)
        )
        if (!strict) {
            ContinueButton(
                visible = visible,
                cooldownMinutes = content.cooldownMinutes,
                showMinutes = !inBlock,
                onClick = onContinue
            )
        }
    }
}

@Composable
private fun IntentChip(text: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = SegundoAireTheme.colors
    val source = remember { MutableInteractionSource() }
    Box(Modifier.heightIn(min = 48.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .focusRing(source, CircleShape)
                .heightIn(min = 36.dp)
                .background(if (selected) colors.wallBtn else colors.wallChip, CircleShape)
                .border(1.dp, if (selected) Color.Transparent else colors.wallChipEdge, CircleShape)
                .selectable(
                    selected = selected,
                    interactionSource = source,
                    indication = null,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = onClick
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = SegundoAireTheme.typography.labelMd,
                color = if (selected) colors.wallBtnOn else colors.wallOn
            )
        }
    }
}

@Composable
private fun OpensAtChip(blockEndsAt: Long, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val hour = Instant.ofEpochMilli(blockEndsAt).atZone(ZoneId.systemDefault()).toLocalTime()
    val time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(hour)
    Row(
        modifier
            .height(32.dp)
            .background(colors.wallChip, CircleShape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(SegundoAireIcons.Lock, contentDescription = null, tint = colors.wallOn, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(text = stringResource(R.string.wall_opens_at, time), style = SegundoAireTheme.typography.labelLg.copy(fontSize = 13.sp), color = colors.wallOn)
    }
}

@Composable
private fun WallButton(text: String, onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val source = remember { MutableInteractionSource() }
    Box(
        modifier
            .fillMaxWidth()
            .height(52.dp)
            .pressScale(source)
            .focusRing(source, CircleShape)
            .background(colors.wallBtn, CircleShape)
            .clickable(interactionSource = source, indication = null, enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = SegundoAireTheme.typography.labelLg, color = colors.wallBtnOn)
    }
}

/**
 * "Continuar en 10…": deshabilitado mientras corre la cuenta, con un tallo que se dibuja
 * en 10 s y una hoja que brota al habilitarse. TalkBack lee la cuenta en el propio botón.
 */
@Composable
private fun ContinueButton(visible: Boolean, cooldownMinutes: Int, showMinutes: Boolean, onClick: () -> Unit) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    var secondsLeft by remember { mutableIntStateOf(GlassWallChoreography.CountdownSeconds) }
    LaunchedEffect(visible) {
        if (!visible) return@LaunchedEffect
        val start = System.currentTimeMillis()
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft = GlassWallChoreography.countdown(System.currentTimeMillis() - start)
        }
    }
    val enabled = visible && secondsLeft == 0
    val stem by animateFloatAsState(GlassWallChoreography.stemProgress(secondsLeft), motion.timer(1000), label = "stem")
    val leafScale by animateFloatAsState(if (enabled) 1f else 0f, motion.bloom(appear = enabled, durationMillis = 500), label = "stemLeaf")
    val stemPath = remember { PathParser().parsePathString(StemPath).toPath() }
    val stemLeaf = remember { PathParser().parsePathString(StemLeafPath).toPath() }
    val measure = remember { PathMeasure().also { it.setPath(stemPath, false) } }
    val partial = remember { Path() }
    val source = remember { MutableInteractionSource() }
    val label = when {
        secondsLeft > 0 -> stringResource(R.string.wall_continue_in, secondsLeft)
        showMinutes -> stringResource(R.string.wall_continue_minutes, cooldownMinutes)
        else -> stringResource(R.string.overlay_continue)
    }

    Row(
        Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .height(48.dp)
            .focusRing(source, CircleShape)
            .clickable(interactionSource = source, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size(28.dp, 16.dp)) {
            scale(density, Offset.Zero) {
                partial.rewind()
                measure.getSegment(0f, measure.length * stem, partial, true)
                drawPath(partial, colors.wallLeaf, style = Stroke(2f, cap = StrokeCap.Round))
                if (leafScale > 0f) scale(leafScale, Offset(14f, 6f)) { drawPath(stemLeaf, colors.wallLeaf) }
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = SegundoAireTheme.typography.labelLg.copy(fontSize = 14.sp),
            color = if (enabled) colors.wallOn else colors.wallMuted
        )
    }
}

// Sobre un fondo que hace de app interceptada: en una preview las animaciones no corren
@Composable
private fun GlassWallSample(content: GlassWallContent) {
    val colors = SegundoAireTheme.colors
    Box(Modifier.fillMaxSize().background(colors.app2)) {
        Box(Modifier.fillMaxSize().background(colors.wallScrim))
        WallContent(
            content = content,
            step = WallStep.Ask,
            fall = { 1f },
            breath = { 0f },
            rise = { 0f },
            reduced = false,
            onBack = {},
            onContinue = {},
            onIntent = {},
            nowMillis = { 0L }
        )
    }
}

private val PreviewMindful = GlassWallContent(GlassWallState.Mindful, appName = "Videos", minutesToday = 23, weekLeaves = 12)

@Preview(name = "GlassWall · Mindful · claro", showSystemUi = true)
@Composable
private fun GlassWallMindfulLightPreview() {
    SegundoAireTheme(darkTheme = false) { GlassWallSample(PreviewMindful) }
}

@Preview(name = "GlassWall · Mindful · oscuro", showSystemUi = true)
@Composable
private fun GlassWallMindfulDarkPreview() {
    SegundoAireTheme(darkTheme = true) { GlassWallSample(PreviewMindful) }
}

@Preview(name = "GlassWall · Strict · oscuro", showSystemUi = true)
@Composable
private fun GlassWallStrictDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        GlassWallSample(PreviewMindful.copy(state = GlassWallState.Strict(blockEndsAt = Long.MAX_VALUE / 2)))
    }
}
