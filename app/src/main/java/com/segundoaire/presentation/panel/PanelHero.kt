package com.segundoaire.presentation.panel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segundoaire.R
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.TodayStats
import com.segundoaire.domain.model.TreeState
import com.segundoaire.presentation.designsystem.theme.Motion
import com.segundoaire.presentation.designsystem.theme.SegundoAireColors
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.toPickIndexes
import com.segundoaire.presentation.tree.TreeGeometry
import com.segundoaire.presentation.tree.drawGrownTree
import com.segundoaire.presentation.tree.rememberTreePaths
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

// Héroe de la referencia: 390 × 300 con el árbol a escala 0.5 y su base en (195, 256)
private val HeroHeight = 300.dp
private const val FrameWidth = 390f
private const val TreeScale = 0.5f
private val TreeBase = Offset(195f, 256f)
/** Colina del héroe en el marco de 390 × 300; también la usa la imagen para compartir. */
internal const val HillPath = "M-10 262 C70 240 140 256 195 252 C255 248 320 232 400 256 L400 300 L-10 300 Z"

// Luz radial: elipse de 120 % × 90 % centrada en (70 %, 0), transparente al 70 %
private const val SkyCenterX = 0.7f
private const val SkyRadiusX = 1.2f
private const val SkyRadiusY = 0.9f
private const val SkyFadeStop = 0.7f

private class FireflyDef(val x: Float, val y: Float, val delayMillis: Int)

private val Fireflies = listOf(
    FireflyDef(60f, 160f, 0), FireflyDef(300f, 120f, 1000), FireflyDef(250f, 220f, 2000), FireflyDef(110f, 90f, 1500)
)
private const val FireflyMillis = 4000
private const val NewLeafMillis = 700
private const val NewLeafStaggerMillis = 120

/**
 * Héroe del Panel (300 dp): luz según la hora, luciérnagas de 20:00 a 6:00, colina y el
 * árbol completo a escala 0.5 con su balanceo de ±0,8° en 7 s. Las flores salen de las
 * metas y cada pausa respetada de la semana agrega una hoja; las nuevas brotan con
 * `Motion.bloom` al entrar y luego se avisa con [onLeavesShown]. Abajo, el dato de
 * minutos recuperados y las pausas y aperturas de hoy.
 *
 * Con reducir movimiento no hay balanceo ni luciérnagas que se muevan, y las hojas nuevas
 * aparecen con un fundido.
 */
@Composable
internal fun PanelHero(
    tree: TreeState,
    stats: TodayStats,
    hour: Int,
    onLeavesShown: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val reduced = motion.reduced
    val paths = rememberTreePaths()
    val hill = remember { PathParser().parsePathString(HillPath).toPath() }
    val flowers = remember(tree.goals) { tree.goals.toPickIndexes() }
    val sky = heroSkyColor(PanelRules.heroLight(hour), colors)
    val fireflies = PanelRules.firefliesVisible(hour)

    val sway: State<Float>
    val fireflyTimes: List<State<Float>>
    if (reduced) {
        sway = remember { mutableFloatStateOf(0f) }
        fireflyTimes = Fireflies.map { remember { mutableFloatStateOf(0.5f) } }
    } else {
        val transition = rememberInfiniteTransition(label = "panelHero")
        sway = transition.animateFloat(-Motion.SwayDegrees, Motion.SwayDegrees, Motion.sway(), label = "sway")
        fireflyTimes = Fireflies.map {
            transition.animateFloat(0f, 1f, Motion.drift(FireflyMillis, it.delayMillis), label = "firefly")
        }
    }

    // Una escala por hueco de hoja: las ya vistas quedan en 1, las nuevas brotan
    val leafScales = remember { List(TreeState.MAX_LEAVES) { Animatable(0f) } }
    val notifyShown by rememberUpdatedState(onLeavesShown)
    LaunchedEffect(tree.leafCount, tree.shownLeaves) {
        leafScales.forEachIndexed { i, scale -> if (i < tree.shownLeaves) scale.snapTo(1f) else if (i >= tree.leafCount) scale.snapTo(0f) }
        if (tree.newLeaves == 0) return@LaunchedEffect
        coroutineScope {
            (tree.shownLeaves until tree.leafCount).forEachIndexed { order, i ->
                launch {
                    leafScales[i].snapTo(0f)
                    leafScales[i].animateTo(
                        1f,
                        motion.bloom(appear = true, durationMillis = NewLeafMillis, delayMillis = order * NewLeafStaggerMillis)
                    )
                }
            }
        }
        notifyShown(tree.leafCount)
    }

    BoxWithConstraints(modifier.fillMaxWidth().height(HeroHeight)) {
        val widthUnits = maxWidth.value
        val treeOffsetX = (widthUnits - FrameWidth) / 2f
        Canvas(Modifier.fillMaxSize().clearAndSetSemantics { }) {
            scale(density, Offset.Zero) {
                drawSky(sky, widthUnits, HeroHeight.value)
                if (fireflies) {
                    translate(left = treeOffsetX) {
                        Fireflies.forEachIndexed { i, f -> drawFirefly(f, fireflyTimes[i].value, colors.firefly) }
                    }
                }
                // La colina va de borde a borde; el árbol queda centrado en su marco de 390
                scale(widthUnits / FrameWidth, 1f, Offset.Zero) { drawPath(hill, colors.hill) }
                translate(treeOffsetX + TreeBase.x, TreeBase.y) {
                    scale(TreeScale, Offset.Zero) {
                        translate(-TreeGeometry.SwayPivot.x, -TreeGeometry.SwayPivot.y) {
                            drawGrownTree(
                                paths = paths,
                                colors = colors,
                                flowers = flowers,
                                leafCount = tree.leafCount,
                                leafScale = { i -> if (reduced) 1f else leafScales[i].value },
                                sway = sway.value
                            )
                        }
                    }
                }
            }
        }
        RecoveredToday(
            minutes = stats.recoveredMinutes,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = SegundoAireTheme.spacing.space5, bottom = SegundoAireTheme.spacing.space1)
        )
        TodayCounts(
            stats = stats,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = SegundoAireTheme.spacing.space5, bottom = SegundoAireTheme.spacing.space2)
        )
    }
}

private fun heroSkyColor(light: HeroLight, colors: SegundoAireColors): Color = when (light) {
    HeroLight.MORNING -> colors.heroSkyMorning
    HeroLight.DAY -> colors.heroSkyDay
    HeroLight.EVENING -> colors.heroSkyEvening
    HeroLight.NIGHT -> colors.heroSkyNight
}

private fun DrawScope.drawSky(color: Color, width: Float, height: Float) {
    if (color.alpha <= 0f) return
    val center = Offset(width * SkyCenterX, 0f)
    val radiusX = width * SkyRadiusX
    // Un degradado circular aplastado en vertical da la elipse del CSS
    scale(1f, (height * SkyRadiusY) / radiusX, center) {
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(0f to color, SkyFadeStop to color.copy(alpha = 0f)),
                center = center,
                radius = radiusX
            ),
            radius = radiusX,
            center = center
        )
    }
}

// Keyframes: (0, 0) opacidad 0.2 → 50 % opacidad 1 → (14, -20) opacidad 0.3
private fun DrawScope.drawFirefly(f: FireflyDef, t: Float, color: Color) {
    val alpha = if (t < 0.5f) 0.2f + 0.8f * (t / 0.5f) else 1f - 0.7f * ((t - 0.5f) / 0.5f)
    val center = Offset(f.x + 2.5f + 14f * t, f.y + 2.5f - 20f * t)
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(0f to color.copy(alpha = 0.6f * alpha), 1f to color.copy(alpha = 0f)),
            center = center,
            radius = 10f
        ),
        radius = 10f,
        center = center
    )
    drawCircle(color.copy(alpha = alpha), 2.5f, center)
}

@Composable
private fun RecoveredToday(minutes: Int, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val unit = stringResource(R.string.panel_recovered_unit)
    val label = stringResource(R.string.panel_recovered_today)
    Column(modifier.semantics(mergeDescendants = true) { }) {
        Text(
            text = buildAnnotatedString {
                append(minutes.toString())
                append(' ')
                withStyle(SpanStyle(fontSize = 22.sp)) { append(unit) }
            },
            style = typography.serif(size = 44, lineHeight = 48).copy(fontFeatureSettings = "tnum"),
            color = colors.onBackground
        )
        Text(text = label, style = typography.labelLg.copy(fontSize = 13.sp), color = colors.onBackgroundMuted)
    }
}

@Composable
private fun TodayCounts(stats: TodayStats, modifier: Modifier = Modifier) {
    Column(modifier.semantics(mergeDescendants = true) { }, horizontalAlignment = Alignment.End) {
        CountLine(stats.pausesRespected, pluralStringResource(R.plurals.panel_pauses_label, stats.pausesRespected))
        CountLine(stats.openingsAvoided, pluralStringResource(R.plurals.panel_openings_label, stats.openingsAvoided))
    }
}

@Composable
private fun CountLine(count: Int, label: String) {
    val colors = SegundoAireTheme.colors
    Row(horizontalArrangement = Arrangement.End) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = colors.onBackground, fontWeight = FontWeight.W700, fontFeatureSettings = "tnum")) {
                    append(count.toString())
                }
                append(' ')
                append(label)
            },
            style = SegundoAireTheme.typography.bodyMd.copy(fontSize = 13.sp),
            color = colors.onBackgroundMuted,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun PanelHeroSample() {
    Box(Modifier.fillMaxWidth().background(SegundoAireTheme.colors.background)) {
        PanelHero(
            tree = TreeState(goals = listOf(Goal.FREE_TIME, Goal.SLEEP), weekLeaves = 3, seenLeaves = 3),
            stats = TodayStats(recoveredMinutes = 47, pausesRespected = 3, openingsAvoided = 12),
            hour = 21,
            onLeavesShown = {},
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Preview(name = "PanelHero · claro")
@Composable
private fun PanelHeroLightPreview() {
    SegundoAireTheme(darkTheme = false) { PanelHeroSample() }
}

@Preview(name = "PanelHero · oscuro")
@Composable
private fun PanelHeroDarkPreview() {
    SegundoAireTheme(darkTheme = true) { PanelHeroSample() }
}
