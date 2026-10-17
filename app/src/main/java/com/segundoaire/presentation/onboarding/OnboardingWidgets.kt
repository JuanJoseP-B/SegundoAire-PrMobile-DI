package com.segundoaire.presentation.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.segundoaire.presentation.designsystem.components.PrimaryButton
import com.segundoaire.presentation.designsystem.components.focusRing
import com.segundoaire.presentation.designsystem.components.pressScale
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass
import com.segundoaire.presentation.onboarding.tree.TreeRules

/** Texto con `*palabra*` convertido en la palabra clave del título, con el estilo de [emphasis]. */
internal fun emphasized(text: String, emphasis: SpanStyle): AnnotatedString = buildAnnotatedString {
    OnboardingRules.splitEmphasis(text).forEach { (part, isEmphasis) ->
        if (isEmphasis) withStyle(emphasis) { append(part) } else append(part)
    }
}

// Posiciones del prototipo (marco de 390 × 844)
internal val StepTextLeft = 24.dp
internal val StepTextTop = 104.dp
internal val StepContentSide = 16.dp
internal val StepButtonBottom = 40.dp
internal val StepButtonHeight = 52.dp

/** Eyebrow en forma de píldora de vidrio: "PASO 2 DE 9". */
@Composable
internal fun StepEyebrow(text: String, modifier: Modifier = Modifier, leadingIcon: String? = null) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    Row(
        modifier
            .height(28.dp)
            .glass(GlassLevel.Level2, SegundoAireTheme.shapes.radiusFull)
            .padding(start = if (leadingIcon != null) 10.dp else 12.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (leadingIcon != null) SvgIcon(leadingIcon, colors.primary, size = 14.dp)
        Text(
            text = text.uppercase(),
            style = typography.labelSmCaps.copy(fontWeight = FontWeight.W600),
            color = colors.onBackgroundMuted,
            maxLines = 1
        )
    }
}

/** Título serif con la palabra clave en cursiva y `primary`. */
@Composable
internal fun StepTitle(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = SegundoAireTheme.typography.serifTitle,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = SegundoAireTheme.colors.onBackground,
    emphasisColor: Color = SegundoAireTheme.colors.primary
) {
    val emphasis = SegundoAireTheme.typography.serifEmphasis.copy(color = emphasisColor)
    Text(
        text = emphasized(text, emphasis),
        modifier = modifier.semantics { heading() },
        style = style,
        color = color,
        textAlign = textAlign
    )
}

@Composable
internal fun StepSubtitle(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        modifier = modifier,
        style = SegundoAireTheme.typography.bodyLg.copy(fontSize = 15.sp, lineHeight = 22.sp),
        color = SegundoAireTheme.colors.onBackgroundMuted,
        textAlign = textAlign
    )
}

/** Eyebrow, título y subtítulo de un paso, en la posición del prototipo (24, 104). */
@Composable
internal fun StepHeader(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleStyle: TextStyle = SegundoAireTheme.typography.serifTitle
) {
    Column(
        modifier
            .offset(x = StepTextLeft, y = StepTextTop)
            .width(342.dp)
    ) {
        StepEyebrow(eyebrow)
        Spacer(Modifier.height(14.dp))
        StepTitle(title, style = titleStyle)
        if (subtitle != null) {
            Spacer(Modifier.height(10.dp))
            StepSubtitle(subtitle)
        }
    }
}

/**
 * Ícono de trazo de 1.75 sobre una rejilla de 24, dibujado desde los datos de un `path` SVG.
 * Con [fill] distinto de `Unspecified` el interior también se pinta.
 */
@Composable
internal fun SvgIcon(
    path: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    fill: Color = Color.Unspecified,
    strokeWidth: Float = 1.75f
) {
    val parsed = remember(path) { PathParser().parsePathString(path).toPath() }
    Canvas(modifier.size(size)) {
        scale(this.size.minDimension / 24f, Offset.Zero) {
            if (fill != Color.Unspecified) drawPath(parsed, fill)
            if (strokeWidth > 0f) drawPath(
                parsed,
                color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

/**
 * Opción de vidrio de Objetivo y Momento: se rellena de `secondary` al elegirla, entra en
 * cascada cuando [shown] pasa a verdadero y se sacude cuando [shaking] lo hace.
 * [role] es `Checkbox` en las metas y `RadioButton` en los momentos.
 */
@Composable
internal fun OnboardingOption(
    label: String,
    iconPath: String,
    selected: Boolean,
    onClick: () -> Unit,
    shown: Boolean,
    index: Int,
    modifier: Modifier = Modifier,
    role: Role = Role.Checkbox,
    dimmed: Boolean = false,
    shaking: Boolean = false,
    filledIcon: Boolean = true,
    trailing: String? = null
) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val typography = SegundoAireTheme.typography
    val shape = SegundoAireTheme.shapes.radiusMd
    val source = remember { MutableInteractionSource() }

    val delay = if (shown) OnboardingRules.cascadeDelayMillis(index) else 0
    val rise = animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = motion.bloom(shown, 520, 520, delay),
        label = "optionRise"
    )
    val visible = animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = motion.fade(300, delay),
        label = "optionVisible"
    )
    val selection = animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = motion.fade(220),
        label = "optionSelection"
    )

    val shake = remember { Animatable(0f) }
    LaunchedEffect(shaking) {
        if (shaking) {
            shake.snapTo(0f)
            shake.animateTo(1f, motion.shake())
        }
    }
    val pop = remember { Animatable(1f) }
    LaunchedEffect(selected) {
        if (selected && !motion.reduced) {
            pop.snapTo(0.6f)
            pop.animateTo(1f, motion.bloom(true, 420))
        }
    }

    val selectionModifier = if (role == Role.RadioButton) {
        Modifier.selectable(
            selected = selected,
            interactionSource = source,
            indication = null,
            role = role,
            onClick = onClick
        )
    } else {
        Modifier.toggleable(
            value = selected,
            interactionSource = source,
            indication = null,
            role = role,
            onValueChange = { onClick() }
        )
    }

    val contentColor = when {
        selected -> colors.onSecondary
        dimmed -> colors.onBackground.copy(alpha = 0.45f)
        else -> colors.onBackground
    }
    val iconColor = if (selected) colors.onSecondary else colors.primary

    Row(
        modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = (1f - rise.value) * 32.dp.toPx()
                translationX = TreeRules.shakeOffset(shake.value) * density
                alpha = visible.value.coerceIn(0f, 1f)
            }
            .pressScale(source)
            .focusRing(source, shape)
            .glass(GlassLevel.Level2, shape)
            .drawBehind {
                val fill = colors.secondary
                drawRect(fill.copy(alpha = fill.alpha * selection.value.coerceIn(0f, 1f)))
            }
            .then(selectionModifier)
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SvgIcon(
            path = iconPath,
            color = iconColor,
            size = 24.dp,
            fill = if (selected && filledIcon) colors.blossom.copy(alpha = 0.9f) else Color.Unspecified,
            modifier = Modifier.graphicsLayer {
                scaleX = pop.value
                scaleY = pop.value
            }
        )
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = typography.titleMd,
            color = contentColor
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = typography.labelLg.copy(fontWeight = FontWeight.W500, letterSpacing = 0.em),
                color = contentColor.copy(alpha = contentColor.alpha * 0.8f)
            )
        }
    }
}

/** Botón principal de un paso, en su sitio del prototipo: 16 dp a cada lado y 40 dp del borde de abajo. */
@Composable
internal fun StepButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    top: Dp = StepButtonTop
) {
    PrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier
            .offset(x = StepContentSide, y = top)
            .width(358.dp)
            .height(StepButtonHeight),
        enabled = enabled
    )
}

internal val StepButtonTop = 844.dp - StepButtonBottom - StepButtonHeight

/** Trazos de los íconos del prototipo (rejilla de 24). */
internal object OnboardingIcons {
    const val Leaf = "M5 19c0-8 6-14 14-14 0 8-6 14-14 14zM5 19l7-7"
    const val Moon = "M20 14.5A8 8 0 1 1 9.5 4a6.5 6.5 0 0 0 10.5 10.5z"
    const val Flame = "M12 3c4 4 5 9 0 18-5-9-4-14 0-18zM12 8v9"
    const val Branch = "M12 3v8M12 11c-3 2-5 5-6 9M12 11c3 2 5 5 6 9M12 11v9"
    const val Drop = "M12 3c4 5 6 8 6 11a6 6 0 0 1-12 0c0-3 2-6 6-11z"
    const val Dawn = "M3 18h18M6 14a6 6 0 0 1 12 0M12 4v3M4.5 8.5l2 2M19.5 8.5l-2 2"
    const val Sun = "M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8zM12 2v2M12 20v2M2 12h2M20 12h2M5 5l1.5 1.5M17.5 17.5L19 19M5 19l1.5-1.5M17.5 6.5L19 5"
    const val Stars = "M12 3l1.5 3.5L17 8l-3.5 1.5L12 13l-1.5-3.5L7 8l3.5-1.5zM18 15l.8 1.7 1.7.8-1.7.8-.8 1.7-.8-1.7-1.7-.8 1.7-.8z"
    const val Sparkle = Stars
    const val Chart = "M4 19V9M10 19V5M16 19v-7M22 19H2"
    const val Window = "M4 6h16v12H4zM8 10h8v4H8z"
    const val Bell = "M6 16V11a6 6 0 0 1 12 0v5l2 2H4zM10 20a2 2 0 0 0 4 0"
    const val Check = "M5 12l5 5 9-10"
    const val SoundBase = "M4 9v6h4l5 4V5L8 9H4z"
    const val SoundOn = "M16 9a4 4 0 0 1 0 6M18.5 6.5a7.5 7.5 0 0 1 0 11"
    const val SoundOff = "M16 9l5 6M21 9l-5 6"
    const val Back = "M15 18l-6-6 6-6"
    const val Globe = "M3 12h18M12 3c3 3 3 15 0 18M12 3c-3 3-3 15 0 18"
    const val Phone = "M11 18h2"
    const val Mail = "M3 7l9 6 9-6"
}
