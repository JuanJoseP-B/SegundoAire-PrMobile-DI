package com.segundoaire.presentation.onboarding.steps

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.theme.FrauncesFontFamily
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.OnboardingRules
import com.segundoaire.presentation.onboarding.StepButton
import com.segundoaire.presentation.onboarding.StepHeader
import com.segundoaire.presentation.onboarding.emphasized
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val RingCount = 7
private const val RingBox = 220f
private const val CounterMillis = 1800
private const val GoodDelayMillis = 2700L
private const val GoodMillis = 1200
private const val RingDelayStepMillis = 120

@Composable
internal fun RevealText(step: Int, total: Int) {
    val typography = SegundoAireTheme.typography
    StepHeader(
        eyebrow = stringResource(R.string.onb_step_of, step, total),
        title = stringResource(R.string.onb_reveal_title),
        titleStyle = typography.serifTitle.copy(fontSize = 28.sp, lineHeight = 34.sp)
    )
}

/**
 * Anillos de crecimiento que se dibujan, el contador de años (1,8 s), la fórmula (1,9 s) y
 * "La buena noticia" (2,6 s). Todo arranca al entrar al paso ([active]) y se reinicia al salir.
 * Con reducir movimiento se muestra ya el valor final.
 */
@Composable
internal fun RevealContent(active: Boolean, usageIndex: Int, onContinue: () -> Unit) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val motion = SegundoAireTheme.motion
    val locale = LocalConfiguration.current.locales[0]
    val format = remember(locale) { OnboardingRules.decimalFormat(locale) }

    val hours = OnboardingRules.usageHours(usageIndex)
    val totalYears = OnboardingRules.projectedYears(hours)
    val goodYears = OnboardingRules.recoveredYears()

    val years = remember { Animatable(0f) }
    val good = remember { Animatable(0f) }
    val rings = remember { List(RingCount) { Animatable(0f) } }
    val captionIn = remember { Animatable(0f) }
    val formulaIn = remember { Animatable(0f) }
    val cardIn = remember { Animatable(0f) }

    LaunchedEffect(active) {
        val all = listOf(years, good, captionIn, formulaIn, cardIn) + rings
        if (!active) {
            all.forEach { it.snapTo(0f) }
            return@LaunchedEffect
        }
        if (motion.reduced) {
            all.forEach { it.snapTo(1f) }
            return@LaunchedEffect
        }
        launch { years.animateTo(1f, motion.count(CounterMillis)) }
        launch {
            delay(GoodDelayMillis)
            good.animateTo(1f, motion.count(GoodMillis))
        }
        rings.forEachIndexed { i, ring ->
            launch { ring.animateTo(1f, motion.grow(true, i * RingDelayStepMillis, CounterMillis)) }
        }
        launch { captionIn.animateTo(1f, motion.rise(600, 1900)) }
        launch { formulaIn.animateTo(1f, motion.rise(600, 2100)) }
        launch { cardIn.animateTo(1f, motion.rise(600, 2600)) }
    }

    // Los anillos pasan a `glow` cuando empieza "la buena noticia"
    val goodStarted by remember { derivedStateOf { good.value > 0f } }
    val ringColor = animateColorAsState(
        targetValue = if (goodStarted) colors.glow else colors.primary,
        animationSpec = motion.fade(900),
        label = "ringColor"
    )

    val shapes = remember {
        List(RingCount) { r ->
            val rx = 34f + r * 12f
            val ry = 30f + r * 11.4f
            RingShape(
                path = Path().apply {
                    arcTo(Rect(RingBox / 2f - rx, RingBox / 2f - ry, RingBox / 2f + rx, RingBox / 2f + ry), 0f, 359.9f, true)
                },
                alpha = 0.25f + r * 0.1f
            )
        }
    }
    val measure = remember { PathMeasure() }
    val scratch = remember { Path() }

    val description = stringResource(R.string.onb_reveal_description, format.format(totalYears))
    Column(
        Modifier
            .offset(y = 200.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(RingBox.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = description
                }
        ) {
            Canvas(Modifier.size(RingBox.dp)) {
                scale(density, Offset.Zero) {
                    shapes.forEachIndexed { i, ring ->
                        val p = rings[i].value.coerceIn(0f, 1f)
                        if (p > 0.002f) {
                            measure.setPath(ring.path, false)
                            scratch.rewind()
                            measure.getSegment(0f, measure.length * p, scratch, true)
                            drawPath(
                                scratch,
                                ringColor.value.copy(alpha = ring.alpha),
                                style = Stroke(width = 2f, cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }
            Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedNumber(
                    text = { format.format(totalYears * years.value) },
                    style = typography.serif(size = 52, lineHeight = 56, tracking = (-0.02).em)
                        .copy(fontFeatureSettings = "tnum"),
                    color = colors.onBackground
                )
                Text(
                    text = stringResource(R.string.onb_reveal_years).uppercase(),
                    style = typography.labelMd.copy(fontSize = 13.sp, letterSpacing = 0.06.em),
                    color = colors.onBackgroundMuted
                )
            }
        }

        Text(
            text = stringResource(R.string.onb_reveal_caption),
            modifier = Modifier
                .padding(start = 32.dp, end = 32.dp, top = 12.dp)
                .rise { captionIn.value },
            style = typography.bodyLg.copy(fontSize = 15.sp, lineHeight = 22.sp),
            color = colors.onBackgroundMuted,
            textAlign = TextAlign.Center
        )
        Text(
            text = OnboardingRules.formula(hours, locale),
            modifier = Modifier
                .padding(top = 6.dp)
                .rise { formulaIn.value },
            style = typography.labelMd.copy(fontWeight = FontWeight.W400, fontFeatureSettings = "tnum"),
            color = colors.onBackgroundMuted
        )
        GlassCard(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 20.dp)
                .fillMaxWidth()
                .rise { cardIn.value },
            contentPadding = PaddingValues(20.dp)
        ) {
            Text(
                text = stringResource(R.string.onb_reveal_good_eyebrow).uppercase(),
                style = typography.labelSmCaps.copy(fontWeight = FontWeight.W600),
                color = colors.onBackgroundMuted
            )
            AnimatedNumber(
                text = { goodSentence(format.format(goodYears * good.value)) },
                style = typography.bodyLg,
                color = colors.onBackground,
                emphasis = SpanStyle(
                    fontFamily = FrauncesFontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W500,
                    color = colors.primary,
                    fontFeatureSettings = "tnum"
                ),
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
    StepButton(text = stringResource(R.string.onboarding_continue), onClick = onContinue)
}

@Composable
private fun goodSentence(years: String): String = stringResource(R.string.onb_reveal_good, years)

private class RingShape(val path: Path, val alpha: Float)

/** Entrada con fundido y subida de 6 dp, leyendo el avance al dibujar. */
private fun Modifier.rise(progress: () -> Float): Modifier = graphicsLayer {
    val p = progress().coerceIn(0f, 1f)
    alpha = p
    translationY = (1f - p) * 6.dp.toPx()
}

/** Texto cuyo contenido cambia cada cuadro: solo esta función se recompone mientras cuenta. */
@Composable
private fun AnimatedNumber(
    text: @Composable () -> String,
    style: androidx.compose.ui.text.TextStyle,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    emphasis: SpanStyle? = null
) {
    val value = text()
    if (emphasis != null) {
        Text(text = emphasized(value, emphasis), modifier = modifier, style = style, color = color)
    } else {
        Text(text = value, modifier = modifier, style = style, color = color)
    }
}
