package com.segundoaire.presentation.blocks

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.dialog
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
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.presentation.designsystem.components.LightStatusBarIcons
import com.segundoaire.presentation.designsystem.components.focusRing
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.FocusColors
import com.segundoaire.presentation.designsystem.theme.Motion
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

// Medidas de la referencia (marco de 390 × 844)
private val RingSize = 240.dp
private const val RingRadius = 120f
private const val RingStroke = 10f
private const val TipHaloRadius = 16f
private const val TipRadius = 7f
private const val TipHaloAlpha = 0.25f
private const val TreeAlpha = 0.22f
private const val TreeFrameHeight = 844f
private const val TreeFrameWidth = 390f
private const val BreathSmall = 0.88f
private const val BreathLarge = 1.06f
private const val TreeTrunk = "M195 844 C192 790 198 760 195 720"

/**
 * Modo enfoque a pantalla completa, siempre oscuro (`FocusColors`, igual en los dos temas):
 * anillo de 240 dp con su punta `glow` que respira, el tiempo en `displayTimer`, la frase
 * y el árbol en silueta respirando abajo. El tiempo es real.
 *
 * Mientras corre, "Terminar bloque" lo detiene ([onEnd]); en Strict hay que mantenerlo.
 * Completado, el botón pasa a "Volver al panel" ([onBackToPanel]). La flecha minimiza
 * ([onMinimize]). Para TalkBack es un diálogo.
 */
@Composable
fun FocusMode(
    state: TimerUiState,
    onEnd: () -> Unit,
    onBackToPanel: () -> Unit,
    onMinimize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = SegundoAireTheme.typography
    val motion = SegundoAireTheme.motion
    val spacing = SegundoAireTheme.spacing
    val finished = state.completedMinutes != null
    val rigorName = stringResource(if (state.rigor == RigorLevel.STRICT) R.string.rigor_strict else R.string.rigor_mindful)
    val title = stringResource(R.string.panel_block_title)

    val breath: State<Float> = if (motion.reduced) {
        remember { mutableFloatStateOf(1f) }
    } else {
        rememberInfiniteTransition(label = "focusBreath")
            .animateFloat(BreathSmall, BreathLarge, Motion.drift(Motion.SeedBreathMillis), label = "breath")
    }

    LightStatusBarIcons(active = true)

    Box(
        modifier
            .fillMaxSize()
            .background(FocusColors.Background)
            // El modo enfoque se lleva todos los toques: nada de la pantalla de abajo responde
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
            .semantics {
                dialog()
                paneTitle = title
            }
    ) {
        FocusTree(breath = breath, modifier = Modifier.align(Alignment.BottomCenter))
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.fillMaxWidth().height(60.dp)) {
                MinimizeButton(onClick = if (finished) onBackToPanel else onMinimize, modifier = Modifier.padding(start = spacing.space2, top = spacing.space2))
            }
            Text(
                text = stringResource(R.string.focus_eyebrow, rigorName).toUpperCase(Locale.current),
                style = typography.labelSmCaps.copy(fontWeight = FontWeight.W600),
                color = FocusColors.Muted
            )
            Spacer(Modifier.height(spacing.space12))
            FocusRing(
                progress = state.progress,
                timeText = BlockRules.clockText(state.remainingSeconds),
                breath = breath
            )
            Spacer(Modifier.height(spacing.space12))
            Text(
                text = stringResource(R.string.focus_quote),
                style = typography.serif(size = 20, lineHeight = 28, weight = FontWeight.W400, italic = true),
                color = FocusColors.Quote,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = spacing.space6)
            )
            Spacer(Modifier.weight(1f))
            val buttonModifier = Modifier.padding(start = spacing.space4, end = spacing.space4, bottom = spacing.space10)
            when {
                finished -> HoldFillButton(
                    text = stringResource(R.string.focus_back_to_panel),
                    onCommit = onBackToPanel,
                    container = FocusColors.Button,
                    content = FocusColors.On,
                    fill = FocusColors.Tip,
                    modifier = buttonModifier,
                    onTap = onBackToPanel
                )
                state.rigor == RigorLevel.STRICT -> HoldFillButton(
                    text = stringResource(R.string.focus_end_hold),
                    onCommit = onEnd,
                    container = FocusColors.Button,
                    content = FocusColors.On,
                    fill = FocusColors.Tip,
                    modifier = buttonModifier
                )
                else -> HoldFillButton(
                    text = stringResource(R.string.focus_end),
                    onCommit = onEnd,
                    container = FocusColors.Button,
                    content = FocusColors.On,
                    fill = FocusColors.Tip,
                    modifier = buttonModifier,
                    onTap = onEnd
                )
            }
        }
    }
}

/** Anillo de 240 dp: pista, avance en `Progress` y la punta que respira; dentro, el tiempo. */
@Composable
private fun FocusRing(progress: Float, timeText: String, breath: State<Float>) {
    val typography = SegundoAireTheme.typography
    val remaining = stringResource(R.string.focus_remaining)
    Box(
        Modifier
            .size(RingSize)
            .semantics(mergeDescendants = true) { },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(RingSize).clearAndSetSemantics { }) {
            scale(density, Offset.Zero) {
                val center = Offset(RingRadius, RingRadius)
                val topLeft = Offset(center.x - RingRadius + RingStroke / 2, center.y - RingRadius + RingStroke / 2)
                val ring = Size((RingRadius - RingStroke / 2) * 2, (RingRadius - RingStroke / 2) * 2)
                val radius = RingRadius - RingStroke / 2
                drawArc(FocusColors.Track, 0f, 360f, false, topLeft, ring, style = Stroke(RingStroke))
                drawArc(FocusColors.Progress, -90f, 360f * progress, false, topLeft, ring, style = Stroke(RingStroke, cap = StrokeCap.Round))
                val tip = BlockRules.tipPosition(progress, center, radius)
                drawCircle(FocusColors.Tip.copy(alpha = TipHaloAlpha), TipHaloRadius * breath.value, tip)
                drawCircle(FocusColors.Tip, TipRadius, tip)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = timeText, style = typography.displayTimer, color = FocusColors.On)
            Text(text = remaining, style = typography.bodyMd.copy(fontSize = 13.sp), color = FocusColors.Muted)
        }
    }
}

@Composable
private fun MinimizeButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val source = remember { MutableInteractionSource() }
    val label = stringResource(R.string.focus_minimize)
    Box(
        modifier
            .size(48.dp)
            .focusRing(source, CircleShape)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Icon(SegundoAireIcons.ChevronDown, contentDescription = null, tint = FocusColors.On, modifier = Modifier.size(24.dp))
    }
}

/** El árbol en silueta al 22 %, abajo, respirando desde la base de la copa. */
@Composable
private fun FocusTree(breath: State<Float>, modifier: Modifier = Modifier) {
    val trunk = remember { PathParser().parsePathString(TreeTrunk).toPath() }
    BoxWithConstraints(modifier.fillMaxWidth().height(260.dp)) {
        val offsetX = (maxWidth.value - TreeFrameWidth) / 2f
        Canvas(Modifier.fillMaxSize().clearAndSetSemantics { }.graphicsLayer { alpha = TreeAlpha }) {
            scale(density, Offset.Zero) {
                // La base del marco de 844 coincide con el borde inferior de la pantalla
                translate(offsetX, size.height / density - TreeFrameHeight) {
                    scale(breath.value, Offset(195f, 700f)) {
                        drawPath(trunk, FocusColors.TreeLight, style = Stroke(6f))
                        drawCircle(FocusColors.TreeLight, 58f, Offset(195f, 690f))
                        drawCircle(FocusColors.TreeDeep, 40f, Offset(150f, 715f))
                        drawCircle(FocusColors.TreeLight, 40f, Offset(240f, 715f))
                        drawCircle(FocusColors.TreeDeep, 36f, Offset(170f, 660f))
                        drawCircle(FocusColors.TreeLight, 36f, Offset(222f, 656f))
                    }
                }
            }
        }
    }
}

private val PreviewRunning = TimerUiState(selectedMinutes = 15, isRunning = true, remainingMillis = 724_000L)

@Preview(name = "Modo enfoque · claro", showSystemUi = true)
@Composable
private fun FocusModeLightPreview() {
    SegundoAireTheme(darkTheme = false) { FocusMode(PreviewRunning, onEnd = {}, onBackToPanel = {}, onMinimize = {}) }
}

@Preview(name = "Modo enfoque · oscuro, Strict", showSystemUi = true)
@Composable
private fun FocusModeDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        FocusMode(PreviewRunning.copy(rigor = RigorLevel.STRICT), onEnd = {}, onBackToPanel = {}, onMinimize = {})
    }
}

@Preview(name = "Modo enfoque · completado", showSystemUi = true)
@Composable
private fun FocusModeCompletedPreview() {
    SegundoAireTheme(darkTheme = true) {
        FocusMode(TimerUiState(remainingMillis = 0L, completedMinutes = 15), onEnd = {}, onBackToPanel = {}, onMinimize = {})
    }
}
