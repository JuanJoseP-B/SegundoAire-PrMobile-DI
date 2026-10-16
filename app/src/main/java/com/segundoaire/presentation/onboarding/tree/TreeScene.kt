package com.segundoaire.presentation.onboarding.tree

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.segundoaire.presentation.designsystem.theme.Motion
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

// ---- Atmósfera: luces, cielo y luciérnagas ----

private class SunDef(val left: Float, val top: Float, val size: Float, val alpha: Float, val millis: Int)

private val Suns = listOf(
    SunDef(-40f, 40f, 220f, 0.55f, 22_000),
    SunDef(220f, 160f, 180f, 0.45f, 28_000)
)

private class FireflyDef(val left: Float, val top: Float, val delayMillis: Int)

private val Fireflies = listOf(
    FireflyDef(60f, 560f, 0), FireflyDef(140f, 640f, 1000), FireflyDef(250f, 590f, 2000),
    FireflyDef(320f, 680f, 500), FireflyDef(200f, 720f, 1500), FireflyDef(30f, 700f, 2500)
)

private const val SunBlur = 28f
private const val FireflyPeriodMillis = 4000

/** El cielo se extiende más allá del marco para que no se vea el borde en pantallas más altas. */
private const val SkyBleed = 300f

/** Luz del suelo y su desvanecimiento hacia arriba, como fracción del alto. */
private val SkyFade = floatArrayOf(0.55f, 0.55f, 0.55f, 0.58f)

/** Color del cielo según el momento elegido (0–3), o `null` sin elección. */
@Composable
internal fun skyColorFor(moment: Int): Color {
    val colors = SegundoAireTheme.colors
    return when (moment) {
        0 -> colors.skyMorning
        1 -> colors.skyAfternoon
        2 -> colors.skyDusk
        3 -> colors.skyNight
        else -> Color.Transparent
    }
}

/**
 * Fondo vivo del prototipo: dos luces que derivan, la luz del suelo del momento elegido
 * y las luciérnagas de la noche. Con [reducedMotion] las luces y luciérnagas se quedan quietas.
 */
@Composable
internal fun SceneAtmosphere(
    moment: Int,
    skyVisible: Boolean,
    firefliesVisible: Boolean,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val motion = SegundoAireTheme.motion
    val colors = SegundoAireTheme.colors
    val sky = animateColorAsState(
        targetValue = if (skyVisible) skyColorFor(moment) else Color.Transparent,
        animationSpec = motion.fade(900),
        label = "sky"
    )
    val fireflyAlpha = animateFloatAsState(
        targetValue = if (firefliesVisible) 1f else 0f,
        animationSpec = motion.fade(900),
        label = "fireflies"
    )
    val skyFade = SkyFade[moment.coerceIn(0, SkyFade.lastIndex)]

    val sunTimes: List<State<Float>>
    val fireflyTimes: List<State<Float>>
    if (reducedMotion) {
        sunTimes = Suns.map { remember { mutableFloatStateOf(0f) } }
        fireflyTimes = Fireflies.map { remember { mutableFloatStateOf(0f) } }
    } else {
        val transition = rememberInfiniteTransition(label = "atmosphere")
        sunTimes = Suns.map { transition.animateFloat(0f, 1f, Motion.drift(it.millis), label = "sun") }
        fireflyTimes = Fireflies.map {
            transition.animateFloat(0f, 1f, Motion.drift(FireflyPeriodMillis, it.delayMillis), label = "firefly")
        }
    }

    Canvas(
        modifier
            .clearAndSetSemantics { }
            .offset(y = (-SkyBleed).dp)
            .requiredSize(TreeRules.FrameWidth.dp, (TreeRules.FrameHeight + 2 * SkyBleed).dp)
    ) {
        scale(density, Offset.Zero) {
            translate(top = SkyBleed) {
                // Luz del suelo: del color elegido abajo a transparente a la altura indicada
                val skyColor = sky.value
                if (skyColor.alpha > 0.001f) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(0f to skyColor.copy(alpha = 0f), 1f to skyColor),
                            startY = TreeRules.FrameHeight * (1f - skyFade),
                            endY = TreeRules.FrameHeight
                        ),
                        topLeft = Offset(0f, -SkyBleed),
                        size = Size(TreeRules.FrameWidth, TreeRules.FrameHeight + 2 * SkyBleed)
                    )
                }
                Suns.forEachIndexed { i, sun -> drawSun(sun, sunTimes[i].value, colors.glass2) }
                val visible = fireflyAlpha.value
                if (visible > 0.001f) {
                    Fireflies.forEachIndexed { i, f -> drawFirefly(f, fireflyTimes[i].value, visible, colors.firefly) }
                }
            }
        }
    }
}

private fun DrawScope.drawSun(sun: SunDef, t: Float, tint: Color) {
    // Keyframes: (0, 0, 1) → (30, 20, 1.15) → (-20, 40, 0.95)
    val first = t < 0.5f
    val f = if (first) t / 0.5f else (t - 0.5f) / 0.5f
    val dx = if (first) 30f * f else 30f + (-20f - 30f) * f
    val dy = if (first) 20f * f else 20f + (40f - 20f) * f
    val s = if (first) 1f + 0.15f * f else 1.15f + (0.95f - 1.15f) * f
    val radius = sun.size / 2f * s
    val center = Offset(sun.left + sun.size / 2f + dx, sun.top + sun.size / 2f + dy)
    val color = tint.copy(alpha = tint.alpha * sun.alpha / 0.58f)
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to color,
                (radius * 0.55f) / (radius + SunBlur) to color,
                1f to color.copy(alpha = 0f)
            ),
            center = center,
            radius = radius + SunBlur
        ),
        radius = radius + SunBlur,
        center = center
    )
}

private fun DrawScope.drawFirefly(f: FireflyDef, t: Float, visible: Float, color: Color) {
    val alpha = (if (t < 0.5f) 0.2f + 0.8f * (t / 0.5f) else 1f + (0.3f - 1f) * ((t - 0.5f) / 0.5f)) * visible
    val center = Offset(f.left + 3f + 18f * t, f.top + 3f - 26f * t)
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(0f to color.copy(alpha = 0.7f * alpha), 1f to color.copy(alpha = 0f)),
            center = center,
            radius = 13f
        ),
        radius = 13f,
        center = center
    )
    drawCircle(color.copy(alpha = alpha), 3f, center)
}

// ---- Colina ----

private val HillEdge = 2400f

@Composable
internal fun OnboardingHill(animations: TreeAnimations, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val fill = remember {
        Path().apply {
            moveTo(-HillEdge, 26f)
            lineTo(-10f, 26f)
            cubicTo(70f, 2f, 140f, 18f, 195f, 14f)
            cubicTo(255f, 10f, 320f, -6f, 400f, 20f)
            lineTo(HillEdge, 20f)
            lineTo(HillEdge, HillEdge)
            lineTo(-HillEdge, HillEdge)
            close()
        }
    }
    val curve = remember { PathParser().parsePathString(TreeGeometry.HillCurve).toPath() }

    Canvas(
        modifier
            .requiredSize(TreeRules.FrameWidth.dp, TreeRules.FrameHeight.dp)
            .graphicsLayer {
                translationY = animations.hillY.value * density
                alpha = animations.hillOpacity.value.coerceIn(0f, 1f)
            }
    ) {
        scale(density, Offset.Zero) {
            drawPath(fill, colors.mint.copy(alpha = 0.42f))
            drawPath(curve, colors.primary.copy(alpha = 0.25f), style = Stroke(width = 1.25f))
        }
    }
}

// ---- Hojas y pétalos que caen ----

private class FallDef(val left: Float, val top: Float, val delayMillis: Int, val durationMillis: Int)

private val FallDefs = listOf(
    FallDef(40f, 120f, 0, 2600), FallDef(120f, 60f, 180, 3000), FallDef(210f, 140f, 90, 2400),
    FallDef(300f, 80f, 260, 2900), FallDef(80f, 220f, 340, 2700), FallDef(250f, 30f, 420, 3200),
    FallDef(340f, 180f, 140, 2500)
)

private const val FallSize = 18f
private const val FallViewBox = 24f

/**
 * Hojas que caen en cada cambio de paso; con el compromiso, pétalos. [gust] sube en cada
 * cambio y reinicia la caída. Con reducir movimiento no cae nada.
 */
@Composable
internal fun FallingLeaves(
    gust: Int,
    petals: Boolean,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val motion = SegundoAireTheme.motion
    val colors = SegundoAireTheme.colors
    val times = FallDefs.map { remember { Animatable(0f) } }
    val leaf = remember { PathParser().parsePathString(TreeGeometry.FallingLeafPath).toPath() }
    val petal = remember { PathParser().parsePathString(TreeGeometry.FallingPetalPath).toPath() }

    LaunchedEffect(gust, reducedMotion) {
        if (gust == 0 || reducedMotion) return@LaunchedEffect
        // Cada hoja arranca de nuevo con su retraso; cancelar el efecto las suelta donde estén
        coroutineScope {
            FallDefs.forEachIndexed { i, def ->
                launch {
                    times[i].snapTo(0f)
                    times[i].animateTo(1f, motion.gust(def.durationMillis, def.delayMillis))
                }
            }
        }
    }

    Canvas(
        modifier
            .clearAndSetSemantics { }
            .requiredSize(TreeRules.FrameWidth.dp, TreeRules.FrameHeight.dp)
    ) {
        scale(density, Offset.Zero) {
            FallDefs.forEachIndexed { i, def ->
                val t = times[i].value
                if (t <= 0f || t >= 1f) return@forEachIndexed
                val pose = TreeRules.fallPose(t)
                if (pose.alpha <= 0.002f) return@forEachIndexed
                val color = when {
                    !petals -> colors.glow
                    i % 2 == 0 -> colors.gold
                    else -> colors.mint
                }
                translate(def.left + pose.x, def.top + pose.y) {
                    rotate(pose.rotation, Offset(FallSize / 2f, FallSize / 2f)) {
                        scale(FallSize / FallViewBox, Offset.Zero) {
                            drawPath(if (petals) petal else leaf, color.copy(alpha = pose.alpha))
                        }
                    }
                }
            }
        }
    }
}

// ---- Progreso: enredadera y puntos de la bienvenida ----

private const val VineWidth = 390f
private const val VineHeight = 40f
private const val VinePath = "M64 20 C100 12 130 28 160 20 S220 12 250 20 S310 28 326 20"

/** Barra de progreso en forma de enredadera: se dibuja de (paso − 2) / 9 en los pasos 3–11. */
@Composable
internal fun GrowthVine(step: Int, modifier: Modifier = Modifier) {
    val motion = SegundoAireTheme.motion
    val colors = SegundoAireTheme.colors
    val vine = remember { PathParser().parsePathString(VinePath).toPath() }
    val measure = remember { androidx.compose.ui.graphics.PathMeasure().also { it.setPath(vine, false) } }
    val partial = remember { Path() }

    val progress = animateFloatAsState(
        targetValue = TreeRules.vineProgress(step),
        animationSpec = motion.grow(true, 200),
        label = "vine"
    )
    val visible = animateFloatAsState(
        targetValue = if (step in TreeRules.NameStep..11) 1f else 0f,
        animationSpec = motion.fade(400),
        label = "vineVisible"
    )

    Canvas(
        modifier
            .clearAndSetSemantics { }
            .offset(y = 16.dp)
            .requiredSize(VineWidth.dp, VineHeight.dp)
            .graphicsLayer { alpha = visible.value }
    ) {
        scale(density, Offset.Zero) {
            drawPath(vine, colors.outlineVariant, style = Stroke(width = 4f, cap = StrokeCap.Round))
            val p = progress.value.coerceIn(0f, 1f)
            if (p > 0.002f) {
                partial.rewind()
                measure.getSegment(0f, measure.length * p, partial, true)
                drawPath(partial, colors.primary, style = Stroke(width = 4f, cap = StrokeCap.Round))
            }
        }
    }
}

/** Tres puntos de la bienvenida: el actual se alarga. */
@Composable
internal fun WelcomeDots(page: Int, visible: Boolean, modifier: Modifier = Modifier) {
    val motion = SegundoAireTheme.motion
    val colors = SegundoAireTheme.colors
    val alpha = animateFloatAsState(if (visible) 1f else 0f, motion.fade(300), label = "dotsVisible")

    Row(
        modifier
            .clearAndSetSemantics { }
            .offset(y = 30.dp)
            .requiredSize(TreeRules.FrameWidth.dp, 4.dp)
            .graphicsLayer { this.alpha = alpha.value },
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
    ) {
        repeat(3) { i ->
            val active = i == page
            val width = animateFloatAsState(if (active) 28f else 12f, motion.dot(), label = "dotWidth")
            val color = animateColorAsState(
                if (active) colors.primary else colors.onBackground.copy(alpha = 0.2f),
                motion.fade(300),
                label = "dotColor"
            )
            Row(
                Modifier
                    .width(width.value.dp)
                    .height(4.dp)
                    .background(color.value, RoundedCornerShape(2.dp))
            ) {}
        }
    }
}


