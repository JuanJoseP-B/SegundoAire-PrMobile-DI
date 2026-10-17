package com.segundoaire.presentation.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.domain.model.ActiveBlock
import com.segundoaire.domain.model.CoachAction
import com.segundoaire.domain.model.CoachMessage
import com.segundoaire.domain.model.CoachTopic
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.GreetingPeriod
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.TodayStats
import com.segundoaire.domain.model.TreeState
import com.segundoaire.presentation.blocks.BlockRules
import com.segundoaire.presentation.designsystem.components.ActionChip
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.components.PermissionBanner
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import kotlinx.coroutines.delay

private val BlockRingSize = 36.dp
private const val BlockRingRadius = 14f
private const val BlockRingStroke = 4f
private const val ClockTickMillis = 250L
private val BlockBarRise = 10.dp

/**
 * Panel de la app principal: saludo según la hora con el nombre en Fraunces, el héroe con
 * el árbol, el bloque en curso (si lo hay), el coach y sus preguntas. Sin estado propio:
 * [PanelRoute] lo conecta. [nowMillis] marca el reloj del bloque en curso.
 */
@Composable
fun PanelScreen(
    state: PanelUiState,
    onFixPermission: () -> Unit,
    onLeavesShown: (Int) -> Unit,
    onActiveBlockClick: () -> Unit,
    onCoachNext: () -> Unit,
    onCoachAsk: (CoachTopic) -> Unit,
    onCoachAction: (CoachAction) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    onTitleLongClick: (() -> Unit)? = null,
    nowMillis: () -> Long = System::currentTimeMillis
) {
    val spacing = SegundoAireTheme.spacing
    val direction = LocalLayoutDirection.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = contentPadding.calculateStartPadding(direction),
                end = contentPadding.calculateEndPadding(direction),
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding() + spacing.space6
            )
    ) {
        PanelHeader(
            greeting = state.greeting,
            name = state.name.trim(),
            onLongClick = onTitleLongClick,
            modifier = Modifier.padding(start = spacing.space5, end = spacing.space5, top = spacing.space4)
        )
        state.missingPermission?.let { missing ->
            PermissionBanner(
                title = stringResource(R.string.banner_title),
                message = stringResource(
                    when (missing) {
                        MissingPermission.ACCESSIBILITY -> R.string.banner_accessibility_disabled
                        MissingPermission.OVERLAY -> R.string.banner_overlay_disabled
                    }
                ),
                actionLabel = stringResource(R.string.banner_action),
                onAction = onFixPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = spacing.space4, end = spacing.space4, top = spacing.space4)
            )
        }
        PanelHero(
            tree = state.tree,
            stats = state.stats,
            hour = state.hour,
            onLeavesShown = onLeavesShown,
            modifier = Modifier.padding(top = spacing.space1)
        )
        ActiveBlockBar(
            block = state.activeBlock,
            nowMillis = nowMillis,
            onClick = onActiveBlockClick,
            modifier = Modifier.padding(start = spacing.space4, end = spacing.space4, top = spacing.space4)
        )
        CoachCard(
            state = state.coach,
            onNext = onCoachNext,
            onAction = onCoachAction,
            modifier = Modifier.padding(start = spacing.space4, end = spacing.space4, top = spacing.space5)
        )
        AskCoach(
            onAsk = onCoachAsk,
            modifier = Modifier.padding(start = spacing.space4, end = spacing.space4, top = spacing.space3 + 2.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PanelHeader(
    greeting: GreetingPeriod,
    name: String,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val hello = stringResource(
        when (greeting) {
            GreetingPeriod.MORNING -> R.string.panel_greeting_morning_anonymous
            GreetingPeriod.AFTERNOON -> R.string.panel_greeting_afternoon_anonymous
            GreetingPeriod.EVENING -> R.string.panel_greeting_evening_anonymous
        }
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { heading() }
            .then(
                // Solo en debug: abre el catálogo de componentes
                if (onLongClick != null) {
                    Modifier.combinedClickable(interactionSource = null, indication = null, onLongClick = onLongClick, onClick = {})
                } else {
                    Modifier
                }
            )
    ) {
        Text(
            text = if (name.isEmpty()) hello else stringResource(R.string.panel_greeting_named, hello),
            style = typography.bodyMd,
            color = colors.onBackgroundMuted
        )
        if (name.isNotEmpty()) {
            Text(
                text = name,
                style = typography.serif(size = 30, lineHeight = 36, tracking = typography.serifTitle.letterSpacing),
                color = colors.onBackground
            )
        }
    }
}

/**
 * Barra del bloque en curso: vidrio, anillo de 36 dp con el avance y "Quedan m:ss".
 * Entra con fundido y una subida de 10 dp; al tocarla abre el bloque.
 */
@Composable
private fun ActiveBlockBar(
    block: ActiveBlock?,
    nowMillis: () -> Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val motion = SegundoAireTheme.motion
    val risePx = with(LocalDensity.current) { BlockBarRise.roundToPx() }
    // Mientras sale, el último bloque conocido sigue dibujado
    var shown by remember { mutableStateOf(block) }
    if (block != null) shown = block
    AnimatedVisibility(
        visible = block != null,
        modifier = modifier,
        enter = fadeIn(motion.rise()) + slideInVertically(motion.rise()) { if (motion.reduced) 0 else risePx },
        exit = fadeOut(motion.fade(200))
    ) {
        shown?.let { ActiveBlockContent(it, nowMillis, onClick) }
    }
}

@Composable
private fun ActiveBlockContent(block: ActiveBlock, nowMillis: () -> Long, onClick: () -> Unit) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val typography = SegundoAireTheme.typography
    val now by produceState(nowMillis(), block) {
        while (true) {
            value = nowMillis()
            delay(ClockTickMillis)
        }
    }
    val progress = block.progress(now)
    val remaining = BlockRules.clockText(block.remainingSeconds(now))

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        level = GlassLevel.Level2,
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = spacing.space4, vertical = spacing.space3)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.size(BlockRingSize)) {
                // Coordenadas del SVG de 36 × 36 de la referencia
                scale(density, Offset.Zero) {
                    val topLeft = Offset(18f - BlockRingRadius, 18f - BlockRingRadius)
                    val ring = Size(BlockRingRadius * 2, BlockRingRadius * 2)
                    drawArc(colors.outlineVariant, 0f, 360f, false, topLeft, ring, style = Stroke(BlockRingStroke))
                    drawArc(colors.primary, -90f, 360f * progress, false, topLeft, ring, style = Stroke(BlockRingStroke, cap = StrokeCap.Round))
                }
            }
            Spacer(Modifier.width(spacing.space3))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.panel_block_title), style = typography.labelLg, color = colors.onBackground)
                Text(
                    text = stringResource(R.string.panel_block_remaining, remaining),
                    style = typography.bodyMd.copy(fontFeatureSettings = "tnum"),
                    color = colors.onBackgroundMuted
                )
            }
            Icon(SegundoAireIcons.ChevronRight, contentDescription = null, tint = colors.onBackground, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AskCoach(onAsk: (CoachTopic) -> Unit, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    Column(modifier) {
        Text(
            text = stringResource(R.string.coach_ask_eyebrow).toUpperCase(Locale.current),
            style = SegundoAireTheme.typography.labelSmCaps.copy(fontWeight = FontWeight.W600),
            color = colors.onBackgroundMuted,
            modifier = Modifier.padding(start = spacing.space1)
        )
        Spacer(Modifier.height(spacing.space1))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
            AskTopics.forEach { (topic, label) ->
                ActionChip(text = stringResource(label), onClick = { onAsk(topic) })
            }
        }
    }
}

private val AskTopics = listOf(
    CoachTopic.WEEK to R.string.coach_ask_week,
    CoachTopic.IDEA to R.string.coach_ask_idea,
    CoachTopic.HARDEST_TIME to R.string.coach_ask_hardest
)

@Composable
private fun PanelScreenSample(state: PanelUiState) {
    PanelScreen(
        state = state,
        onFixPermission = {},
        onLeavesShown = {},
        onActiveBlockClick = {},
        onCoachNext = {},
        onCoachAsk = {},
        onCoachAction = {},
        modifier = Modifier.background(SegundoAireTheme.colors.background),
        nowMillis = { PreviewNow }
    )
}

private const val PreviewNow = 1_000_000L

private val PreviewState = PanelUiState(
    greeting = GreetingPeriod.AFTERNOON,
    hour = 15,
    name = "Juan",
    stats = TodayStats(recoveredMinutes = 47, pausesRespected = 3, openingsAvoided = 12),
    tree = TreeState(goals = listOf(Goal.FREE_TIME, Goal.SLEEP), weekLeaves = 2, seenLeaves = 2),
    activeBlock = ActiveBlock(
        startedAt = PreviewNow - 3 * ActiveBlock.MILLIS_PER_MINUTE,
        endsAt = PreviewNow + 12 * ActiveBlock.MILLIS_PER_MINUTE,
        rigor = RigorLevel.MINDFUL,
        durationMinutes = 15
    ),
    coach = CoachUiState(message = CoachMessage.PausesToday(today = 3, yesterday = 1), isThinking = false, revision = 1)
)

@Preview(name = "Panel · claro", showSystemUi = true)
@Composable
private fun PanelScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) { PanelScreenSample(PreviewState) }
}

@Preview(name = "Panel · oscuro", showSystemUi = true)
@Composable
private fun PanelScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) { PanelScreenSample(PreviewState) }
}

@Preview(name = "Panel · noche, sin bloque · oscuro", showSystemUi = true)
@Composable
private fun PanelScreenNightDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        PanelScreenSample(PreviewState.copy(hour = 22, greeting = GreetingPeriod.EVENING, activeBlock = null, coach = CoachUiState()))
    }
}
