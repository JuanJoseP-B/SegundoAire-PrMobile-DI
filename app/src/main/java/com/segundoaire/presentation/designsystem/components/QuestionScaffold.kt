package com.segundoaire.presentation.designsystem.components

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.GlassBackdrop
import com.segundoaire.presentation.designsystem.theme.SegundoAireMotion
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glassBackdrop

private val ProgressSegmentHeight = 4.dp
private val BackIconSize = 24.dp

// Opacidad máxima de las manchas del fondo: con ella el texto secundario
// sigue cumpliendo AA encima en claro y en oscuro
private const val BackdropTintAlpha = 0.35f

/**
 * Esqueleto de un paso del onboarding: arriba, volver y el progreso segmentado;
 * en medio, el [title], el [subtitle] y el [content], que se desplazan si no
 * caben; abajo, la [action], fija y por encima del teclado.
 *
 * Con [totalSteps] en 0 no se muestra el progreso y sin [onBack] no hay botón
 * de volver, pero la cabecera conserva su alto para que el título no salte de
 * un paso a otro.
 *
 * Con [scrollContent] en `false` el área central no se desplaza y [content]
 * recibe el alto restante (`Modifier.weight(1f)`): para pantallas con su propia
 * lista perezosa, que no puede vivir dentro de un `verticalScroll`.
 */
@Composable
fun QuestionScaffold(
    action: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    step: Int = 0,
    totalSteps: Int = 0,
    onBack: (() -> Unit)? = null,
    scrollContent: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val typography = SegundoAireTheme.typography

    GlassBackdrop {
        Box(modifier.fillMaxSize()) {
            QuestionBackground()
            Column(
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = ButtonMinSize)
                        .padding(horizontal = spacing.space2),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        BackButton(onBack)
                    } else {
                        // Sin botón, el progreso arranca en el margen de la pantalla
                        Spacer(Modifier.width(spacing.space2))
                    }
                    if (totalSteps > 0) {
                        SegmentedProgress(
                            step = step,
                            totalSteps = totalSteps,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = spacing.space2)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .then(if (scrollContent) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                        .padding(horizontal = spacing.space4)
                        .padding(top = spacing.space6, bottom = spacing.space6)
                ) {
                    if (title != null) {
                        Text(
                            text = title,
                            style = typography.headlineMd,
                            color = colors.onBackground,
                            modifier = Modifier.semantics { heading() }
                        )
                    }
                    if (subtitle != null) {
                        Spacer(Modifier.height(spacing.space2))
                        Text(text = subtitle, style = typography.bodyMd, color = colors.onBackgroundMuted)
                    }
                    if (title != null || subtitle != null) {
                        Spacer(Modifier.height(spacing.space8))
                    }
                    content()
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.space4)
                        .padding(bottom = spacing.space4),
                    contentAlignment = Alignment.Center
                ) {
                    action()
                }
            }
        }
    }
}

/** [QuestionScaffold] con la acción habitual: un [PrimaryButton] a todo el ancho. */
@Composable
fun QuestionScaffold(
    primaryLabel: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    title: String? = null,
    subtitle: String? = null,
    step: Int = 0,
    totalSteps: Int = 0,
    onBack: (() -> Unit)? = null,
    scrollContent: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    QuestionScaffold(
        action = {
            PrimaryButton(
                text = primaryLabel,
                onClick = onPrimary,
                modifier = Modifier.fillMaxWidth(),
                enabled = primaryEnabled
            )
        },
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        step = step,
        totalSteps = totalSteps,
        onBack = onBack,
        scrollContent = scrollContent,
        content = content
    )
}

/**
 * Entrada de `Motion.step`: desplazamiento de 24 dp más fundido. [direction] es
 * 1 al avanzar y -1 al volver (invertido en idiomas de derecha a izquierda).
 * Con reducir movimiento el desplazamiento es 0 y solo queda el fundido.
 */
fun stepEnterTransition(motion: SegundoAireMotion, offsetPx: Int, direction: Int): EnterTransition =
    slideInHorizontally(motion.step()) { offsetPx * direction } + fadeIn(motion.step())

/** Salida de `Motion.step`, hacia el lado contrario al de [stepEnterTransition]. */
fun stepExitTransition(motion: SegundoAireMotion, offsetPx: Int, direction: Int): ExitTransition =
    slideOutHorizontally(motion.step()) { -offsetPx * direction } + fadeOut(motion.step())

/** Transición `Motion.step` completa para un `AnimatedContent` dentro de un paso. */
fun stepContentTransform(motion: SegundoAireMotion, offsetPx: Int, direction: Int): ContentTransform =
    ContentTransform(
        targetContentEnter = stepEnterTransition(motion, offsetPx, direction),
        initialContentExit = stepExitTransition(motion, offsetPx, direction),
        sizeTransform = SizeTransform(clip = false) { _, _ -> motion.step() }
    )

@Composable
private fun BackButton(onBack: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val stateLayer by animateStateLayer(source, enabled = true)
    val shape = SegundoAireTheme.shapes.radiusFull
    Box(
        modifier = Modifier
            .size(ButtonMinSize)
            .pressScale(source)
            .focusRing(source, shape)
            .clip(shape)
            .drawBehind { drawRect(stateLayer) }
            .clickable(
                interactionSource = source,
                indication = null,
                role = Role.Button,
                onClick = onBack
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = SegundoAireIcons.ArrowBack,
            contentDescription = stringResource(R.string.onboarding_back),
            tint = SegundoAireTheme.colors.onBackground,
            modifier = Modifier.size(BackIconSize)
        )
    }
}

/** Un segmento por paso; los ya alcanzados se llenan de `primary` con `Motion.step`. */
@Composable
private fun SegmentedProgress(
    step: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val shape = SegundoAireTheme.shapes.radiusFull
    val description = stringResource(R.string.onboarding_progress, step, totalSteps)

    Row(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = description
            progressBarRangeInfo = ProgressBarRangeInfo(
                current = step.toFloat(),
                range = 0f..totalSteps.toFloat(),
                steps = (totalSteps - 1).coerceAtLeast(0)
            )
        },
        horizontalArrangement = Arrangement.spacedBy(SegundoAireTheme.spacing.space1)
    ) {
        repeat(totalSteps) { index ->
            val fill by animateFloatAsState(
                targetValue = if (index < step) 1f else 0f,
                animationSpec = motion.step(),
                label = "progressSegment"
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(ProgressSegmentHeight)
                    .clip(shape)
                    .background(colors.outlineVariant)
                    .drawBehind {
                        if (motion.reduced) {
                            // Sin desplazamiento: el segmento se funde
                            drawRect(colors.primary.copy(alpha = fill.coerceIn(0f, 1f)))
                        } else {
                            drawFill(colors.primary, fill.coerceIn(0f, 1f))
                        }
                    }
            )
        }
    }
}

// El relleno avanza en el sentido de lectura
private fun DrawScope.drawFill(color: Color, fraction: Float) {
    val width = size.width * fraction
    val left = if (layoutDirection == LayoutDirection.Rtl) size.width - width else 0f
    drawRect(color = color, topLeft = Offset(left, 0f), size = Size(width, size.height))
}

// Manchas suaves detrás del contenido para que el blur del vidrio tenga qué desenfocar
@Composable
private fun QuestionBackground() {
    val colors = SegundoAireTheme.colors
    Box(
        Modifier
            .fillMaxSize()
            .glassBackdrop()
            .background(colors.background)
            .drawBehind {
                drawBlob(
                    color = colors.surfaceGlass,
                    center = Offset(size.width * 0.9f, size.height * 0.25f),
                    radius = size.width * 0.6f
                )
                drawBlob(
                    color = colors.surfaceGlass,
                    center = Offset(size.width * 0.05f, size.height * 0.75f),
                    radius = size.width * 0.45f
                )
            }
    )
}

private fun DrawScope.drawBlob(color: Color, center: Offset, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = BackdropTintAlpha), color.copy(alpha = 0f)),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

@Composable
private fun QuestionScaffoldSample() {
    QuestionScaffold(
        primaryLabel = stringResource(R.string.onboarding_continue),
        onPrimary = {},
        title = stringResource(R.string.onboarding_goals_title),
        subtitle = stringResource(R.string.onboarding_goals_subtitle, 2),
        step = 2,
        totalSteps = 8,
        onBack = {}
    ) {
        ChoiceChipGroup(
            options = listOf(
                stringResource(R.string.onboarding_goal_focus),
                stringResource(R.string.onboarding_goal_sleep),
                stringResource(R.string.onboarding_goal_presence)
            ),
            selected = setOf(1),
            onSelectionChange = {},
            maxSelected = 2
        )
    }
}

@Preview(name = "QuestionScaffold · claro")
@Composable
private fun QuestionScaffoldLightPreview() {
    SegundoAireTheme(darkTheme = false) { QuestionScaffoldSample() }
}

@Preview(name = "QuestionScaffold · oscuro")
@Composable
private fun QuestionScaffoldDarkPreview() {
    SegundoAireTheme(darkTheme = true) { QuestionScaffoldSample() }
}
