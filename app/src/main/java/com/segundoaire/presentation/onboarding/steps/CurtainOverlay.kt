package com.segundoaire.presentation.onboarding.steps

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.emphasized
import com.segundoaire.presentation.onboarding.tree.TreeRules

/** Dónde está el telón: arriba y recogido, cubriéndolo todo, o retirado a 270 dp con borde curvo. */
internal enum class CurtainState { Up, Cover, Rest }

private const val CoverHeight = 900f
private const val RestHeight = 270f
private const val Bleed = 300f
private const val CurveHeight = 80f
private const val TreeTopHidden = 600f
private const val TreeTopRest = 70f
private const val TreeSize = 190f

/**
 * Telón del login y del cierre ([done] es el cierre, con "Todo listo"): baja y cubre en 900 ms, a los 850 ms se retira a 270 dp con un
 * borde curvo, y en el cierre vuelve a cubrir todo con "Todo listo". Se dibuja por encima de la
 * capa del login; con [state] en `Cover` se lleva los toques para que nada quede accesible debajo.
 */
@Composable
internal fun CurtainOverlay(
    state: CurtainState,
    done: Boolean,
    name: String,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val motion = SegundoAireTheme.motion

    val height = animateFloatAsState(
        targetValue = when (state) {
            CurtainState.Up -> 0f
            CurtainState.Cover -> CoverHeight
            CurtainState.Rest -> RestHeight
        },
        animationSpec = motion.curtain(),
        label = "curtainHeight"
    )
    // 0 = borde casi plano (arriba y cubriendo), 1 = borde curvo (retirado)
    val curve = animateFloatAsState(
        targetValue = if (state == CurtainState.Rest) 1f else 0f,
        animationSpec = motion.curtain(),
        label = "curtainCurve"
    )
    val curveAlpha = animateFloatAsState(
        targetValue = if (state == CurtainState.Up) 0f else 1f,
        animationSpec = motion.fade(200, if (state == CurtainState.Up) 700 else 0),
        label = "curtainCurveAlpha"
    )
    val treeIn = animateFloatAsState(
        targetValue = if (state == CurtainState.Rest) 1f else 0f,
        animationSpec = motion.curtain(),
        label = "curtainTree"
    )
    val treeAlpha = animateFloatAsState(
        targetValue = if (state == CurtainState.Rest) 1f else 0f,
        animationSpec = motion.fade(500),
        label = "curtainTreeAlpha"
    )
    val doneIn = animateFloatAsState(
        targetValue = if (done) 1f else 0f,
        animationSpec = motion.fade(600, if (done) 700 else 0),
        label = "curtainDone"
    )

    val curveBase = remember { Path() }
    val onVisible by remember { derivedStateOf { state != CurtainState.Up || height.value > 1f } }
    if (!onVisible) return

    Box(
        modifier
            .fillMaxSize()
            .then(
                if (state == CurtainState.Cover) {
                    Modifier.pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent() } }
                } else {
                    Modifier
                }
            )
    ) {
        // Telón y borde curvo; se extienden por encima del marco para cubrir la barra de estado
        Canvas(
            Modifier
                .offset(y = (-Bleed).dp)
                .requiredSize(TreeRules.FrameWidth.dp, (CoverHeight + Bleed + CurveHeight).dp)
        ) {
            scale(density, Offset.Zero) {
                translate(top = Bleed) {
                    val h = height.value
                    drawRect(colors.secondaryDeep, Offset(0f, -Bleed), androidx.compose.ui.geometry.Size(390f, h + Bleed))
                    val alpha = curveAlpha.value
                    if (alpha > 0.001f) {
                        curveBase.rewind()
                        // De un borde casi plano (V4, C 300 30 140 20 0 30) a uno curvo (V10, C 300 80 140 40 0 64)
                        val t = curve.value
                        curveBase.moveTo(0f, h - 1f)
                        curveBase.lineTo(390f, h - 1f)
                        curveBase.lineTo(390f, h - 1f + lerp(4f, 10f, t))
                        curveBase.cubicTo(
                            300f, h - 1f + lerp(30f, 80f, t),
                            140f, h - 1f + lerp(20f, 40f, t),
                            0f, h - 1f + lerp(30f, 64f, t)
                        )
                        curveBase.close()
                        drawPath(curveBase, colors.secondaryDeep.copy(alpha = colors.secondaryDeep.alpha * alpha))
                    }
                }
            }
        }

        // Contenido del telón: solo se ve donde el telón ya llegó
        Box(
            Modifier
                .fillMaxSize()
                .drawWithContent {
                    clipRect(bottom = height.value * density) {
                        this@drawWithContent.drawContent()
                    }
                }
        ) {
            Column(Modifier.offset(x = 24.dp, y = 72.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = typography.serif(size = 30, lineHeight = 36, tracking = (-0.01).em),
                    color = colors.onSecondary
                )
                Text(
                    text = stringResource(R.string.onb_curtain_tagline),
                    modifier = Modifier.padding(top = 4.dp),
                    style = typography.bodyMd,
                    color = colors.onSecondary.copy(alpha = colors.onSecondary.alpha * 0.78f)
                )
            }

            CurtainTree(
                Modifier
                    .offset(x = (TreeRules.FrameWidth - 8f - TreeSize).dp)
                    .graphicsLayer {
                        translationY = (TreeTopHidden + (TreeTopRest - TreeTopHidden) * treeIn.value) * density
                        alpha = treeAlpha.value
                    }
            )

            // Solo existe durante el cierre: si no, su botón invisible taparía los campos del login
            if (done) {
                DoneBlock(
                    name = name,
                    visible = { doneIn.value },
                    onFinish = onFinish
                )
            }
        }
    }
}

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

/** Silueta del árbol sobre el telón. */
@Composable
private fun CurtainTree(modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val trunk = remember {
        Path().apply {
            moveTo(95f, 190f)
            cubicTo(93f, 160f, 97f, 130f, 95f, 100f)
        }
    }
    Canvas(modifier.requiredSize(TreeSize.dp)) {
        scale(density, Offset.Zero) {
            drawPath(
                trunk,
                colors.mint.copy(alpha = 0.6f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 5f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
            drawCircle(colors.mint.copy(alpha = 0.32f), 44f, Offset(95f, 70f))
            drawCircle(colors.glow.copy(alpha = 0.45f), 32f, Offset(58f, 92f))
            drawCircle(colors.mint.copy(alpha = 0.25f), 32f, Offset(132f, 92f))
            drawCircle(colors.glow.copy(alpha = 0.4f), 28f, Offset(76f, 52f))
            drawCircle(colors.mint.copy(alpha = 0.3f), 28f, Offset(118f, 50f))
            drawCircle(colors.blossom.copy(alpha = 0.8f), 6f, Offset(70f, 66f))
            drawCircle(colors.blossom.copy(alpha = 0.8f), 6f, Offset(124f, 78f))
        }
    }
}

/** "Todo listo, {nombre}." y el botón que cierra el onboarding. */
@Composable
private fun DoneBlock(name: String, visible: () -> Float, onFinish: () -> Unit) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val source = remember { MutableInteractionSource() }
    val shape = SegundoAireTheme.shapes.radiusFull
    val emphasis = typography.serifEmphasis.copy(color = colors.onSecondary)

    Column(
        Modifier
            .offset(x = 24.dp, y = 300.dp)
            .width(342.dp)
            .graphicsLayer {
                val p = visible().coerceIn(0f, 1f)
                alpha = p
                translationY = (1f - p) * 12.dp.toPx()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emphasized(stringResource(R.string.onb_done_title, name), emphasis),
            style = typography.serif(size = 40, lineHeight = 46),
            color = colors.onSecondary,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.onb_done_body),
            modifier = Modifier.padding(top = 12.dp),
            style = typography.bodyLg.copy(fontSize = 15.sp, lineHeight = 22.sp),
            color = colors.onSecondary.copy(alpha = colors.onSecondary.alpha * 0.8f),
            textAlign = TextAlign.Center
        )
        Box(
            Modifier
                .padding(top = 32.dp)
                .height(52.dp)
                .border(1.dp, colors.onSecondary.copy(alpha = colors.onSecondary.alpha * 0.5f), shape)
                .clickable(interactionSource = source, indication = null, role = Role.Button) {
                    if (visible() > 0.9f) onFinish()
                }
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.onboarding_continue),
                style = typography.labelLg.copy(fontWeight = FontWeight.W600),
                color = colors.onSecondary
            )
        }
    }
}
