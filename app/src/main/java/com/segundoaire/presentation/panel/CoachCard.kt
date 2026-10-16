package com.segundoaire.presentation.panel

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.domain.model.CoachAction
import com.segundoaire.domain.model.CoachMessage
import com.segundoaire.domain.model.Goal
import com.segundoaire.presentation.designsystem.components.ActionChip
import com.segundoaire.presentation.designsystem.components.RevealText
import com.segundoaire.presentation.designsystem.components.focusRing
import com.segundoaire.presentation.designsystem.components.pressScale
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.Motion
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass
import kotlin.math.hypot

// Medidas de la referencia
private val OuterRadius = 26.dp
private val InnerRadius = 24.dp
private val EdgeWidth = 1.5.dp
private val SeedSize = 36.dp
private val MessageMinHeight = 96.dp
private val DotSize = 6.dp
private val DotRise = 3.dp
private val SkeletonHeight = 12.dp
private val SkeletonWidths = floatArrayOf(1f, 0.86f, 0.6f)
private val ActionsRise = 6.dp
private const val ActionsFadeMillis = 400
private const val SeedSmall = 0.88f
private const val SeedLarge = 1.06f
private const val AccentSoftAlpha = 0.14f
private const val SeedLeafPath = "M18 9 C24 14 24 22 18 28 C12 22 12 14 18 9Z"

/**
 * El coach, protagonista del Panel. Mientras piensa (1,4 s), un brillo cónico de `glow`
 * gira en el borde, tres puntos laten y hay líneas skeleton. Después el mensaje se revela
 * por caracteres en Fraunces 20 / 28 y, solo al terminar, entran sus dos acciones.
 * TalkBack recibe el texto completo de una vez (región viva educada).
 */
@Composable
internal fun CoachCard(
    state: CoachUiState,
    onNext: () -> Unit,
    onAction: (CoachAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val spacing = SegundoAireTheme.spacing
    val glowColor = colors.glow
    val thinking = state.isThinking || state.message == null

    val rotation: State<Float> = if (thinking && !motion.reduced) {
        rememberInfiniteTransition(label = "coachThink").animateFloat(0f, 360f, Motion.think(), label = "angle")
    } else {
        remember { mutableFloatStateOf(0f) }
    }
    val sweep = remember(glowColor) {
        Brush.sweepGradient(
            0f to Color.Transparent,
            0.7f to Color.Transparent,
            0.85f to glowColor,
            1f to Color.Transparent
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OuterRadius))
            .drawBehind {
                drawRect(colors.glassEdgeHi)
                if (thinking) {
                    // Más grande que la tarjeta: al girar cubre también las esquinas
                    rotate(rotation.value) { drawCircle(sweep, radius = hypot(size.width, size.height)) }
                }
            }
            .padding(EdgeWidth)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(colors.aiCard, RoundedCornerShape(InnerRadius))
                .padding(spacing.space5)
        ) {
            CoachHeader(thinking = thinking, onNext = onNext)
            Spacer(Modifier.height(spacing.space3 + 2.dp))
            CoachMessageArea(state = state, thinking = thinking, onAction = onAction)
        }
    }
}

@Composable
private fun CoachHeader(thinking: Boolean, onNext: () -> Unit) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    Row(verticalAlignment = Alignment.CenterVertically) {
        CoachSeed()
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.coach_title),
                style = typography.labelLg.copy(fontWeight = FontWeight.W700),
                color = colors.onBackground
            )
            Text(
                text = stringResource(if (thinking) R.string.coach_status_thinking else R.string.coach_status_ready),
                style = typography.labelMd.copy(fontWeight = FontWeight.W400),
                color = colors.onBackgroundMuted
            )
        }
        RefreshButton(onClick = onNext)
    }
}

// Semilla del coach: respira de 0.88 a 1.06 cada 4 s
@Composable
private fun CoachSeed() {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val leaf = remember { PathParser().parsePathString(SeedLeafPath).toPath() }
    val breath: State<Float> = if (motion.reduced) {
        remember { mutableFloatStateOf(1f) }
    } else {
        rememberInfiniteTransition(label = "coachSeed")
            .animateFloat(SeedSmall, SeedLarge, Motion.drift(Motion.SeedBreathMillis), label = "seed")
    }
    Canvas(
        Modifier
            .size(SeedSize)
            .graphicsLayer {
                scaleX = breath.value
                scaleY = breath.value
            }
    ) {
        // Coordenadas del SVG de 36 × 36 de la referencia
        scale(density, Offset.Zero) {
            drawCircle(colors.glow.copy(alpha = AccentSoftAlpha), 17f, Offset(18f, 18f))
            drawPath(leaf, colors.accent)
            drawLine(colors.aiCard, Offset(18f, 14f), Offset(18f, 24f), strokeWidth = 1.5f, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun RefreshButton(onClick: () -> Unit) {
    val colors = SegundoAireTheme.colors
    val source = remember { MutableInteractionSource() }
    val label = stringResource(R.string.coach_another)
    Box(
        modifier = Modifier
            .size(48.dp)
            .pressScale(source)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .focusRing(source, CircleShape)
                .size(36.dp)
                .glass(GlassLevel.Level2, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(SegundoAireIcons.Refresh, contentDescription = null, tint = colors.onBackground, modifier = Modifier.size(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CoachMessageArea(state: CoachUiState, thinking: Boolean, onAction: (CoachAction) -> Unit) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val motion = SegundoAireTheme.motion
    val spacing = SegundoAireTheme.spacing
    val message = state.message
    val fullText = if (thinking || message == null) stringResource(R.string.coach_thinking_description) else coachText(message)
    var revealed by remember(state.revision) { mutableStateOf(false) }
    val showActions = !thinking && revealed

    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = MessageMinHeight)
            // Una sola lectura del texto entero cuando cambia, no letra por letra
            .clearAndSetSemantics {
                contentDescription = fullText
                liveRegion = LiveRegionMode.Polite
            }
    ) {
        if (thinking || message == null) {
            ThinkingPlaceholder()
        } else {
            key(state.revision) {
                RevealText(
                    text = fullText,
                    style = typography.serif(size = 20, lineHeight = 28, weight = FontWeight.W400),
                    color = colors.onBackground,
                    onRevealed = { revealed = true }
                )
            }
        }
    }

    val actionsAlpha by animateFloatAsState(if (showActions) 1f else 0f, motion.fade(ActionsFadeMillis), label = "coachActions")
    val risePx = with(LocalDensity.current) { ActionsRise.toPx() }
    if (message != null) {
        FlowRow(
            modifier = Modifier
                .padding(top = spacing.space4)
                .graphicsLayer {
                    alpha = actionsAlpha
                    translationY = if (motion.reduced) 0f else risePx * (1f - actionsAlpha)
                }
                // Invisibles no se pueden tocar ni se anuncian
                .then(if (showActions) Modifier else Modifier.clearAndSetSemantics { }),
            horizontalArrangement = Arrangement.spacedBy(spacing.space2)
        ) {
            ActionChip(
                text = coachActionLabel(message, message.primary),
                onClick = { onAction(message.primary) },
                solid = true,
                enabled = showActions
            )
            ActionChip(
                text = coachActionLabel(message, message.secondary),
                onClick = { onAction(message.secondary) },
                enabled = showActions
            )
        }
    }
}

// Tres puntos que laten con desfase de 150 ms y tres líneas skeleton que pulsan
@Composable
private fun ThinkingPlaceholder() {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val spacing = SegundoAireTheme.spacing
    val dots: List<State<Float>>
    val pulse: State<Float>
    if (motion.reduced) {
        dots = List(3) { remember { mutableFloatStateOf(1f) } }
        pulse = remember { mutableFloatStateOf(1f) }
    } else {
        val transition = rememberInfiniteTransition(label = "coachThinking")
        dots = List(3) { i ->
            transition.animateFloat(
                0f, 1f,
                Motion.drift(Motion.CoachDotMillis, i * Motion.CoachDotStaggerMillis),
                label = "dot"
            )
        }
        pulse = transition.animateFloat(0f, 1f, Motion.drift(Motion.SkeletonMillis), label = "skeleton")
    }
    val risePx = with(LocalDensity.current) { DotRise.toPx() }

    Column(Modifier.padding(top = spacing.space1), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            dots.forEach { t ->
                Box(
                    Modifier
                        .size(DotSize)
                        .graphicsLayer {
                            alpha = 0.25f + 0.75f * t.value
                            translationY = -risePx * t.value
                        }
                        .background(colors.accent, CircleShape)
                )
            }
        }
        SkeletonWidths.forEach { fraction ->
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .height(SkeletonHeight)
                    .graphicsLayer { alpha = 0.5f + 0.5f * pulse.value }
                    .background(colors.outlineVariant, RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
private fun CoachCardSample(state: CoachUiState) {
    Box(
        Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(SegundoAireTheme.spacing.space4)
    ) {
        CoachCard(state = state, onNext = {}, onAction = {})
    }
}

private val PreviewReady = CoachUiState(message = CoachMessage.GoalIdea(Goal.SLEEP), isThinking = false, revision = 1)

@Preview(name = "CoachCard · listo · claro")
@Composable
private fun CoachCardReadyLightPreview() {
    SegundoAireTheme(darkTheme = false) { CoachCardSample(PreviewReady) }
}

@Preview(name = "CoachCard · listo · oscuro")
@Composable
private fun CoachCardReadyDarkPreview() {
    SegundoAireTheme(darkTheme = true) { CoachCardSample(PreviewReady) }
}

@Preview(name = "CoachCard · pensando · claro")
@Composable
private fun CoachCardThinkingLightPreview() {
    SegundoAireTheme(darkTheme = false) { CoachCardSample(CoachUiState()) }
}

@Preview(name = "CoachCard · pensando · oscuro")
@Composable
private fun CoachCardThinkingDarkPreview() {
    SegundoAireTheme(darkTheme = true) { CoachCardSample(CoachUiState()) }
}
