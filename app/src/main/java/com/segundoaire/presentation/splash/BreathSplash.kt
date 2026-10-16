package com.segundoaire.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import kotlinx.coroutines.delay

/**
 * El Respiro: pantalla de carga de 16 s que se reproduce una vez por arranque.
 * Se dibuja como overlay sobre la app (que ya está compuesta debajo) y termina con
 * un fundido que la revela. Un toque salta directo a la fase 6.
 *
 * Con "reducir movimiento" la secuencia es forma quieta, título y bienvenida, por
 * fundidos de 150 ms, en 1.5 s.
 *
 * [onExitStarted] avisa del comienzo de la fase 6, para que la pantalla de abajo
 * empiece a entrar mientras el overlay se funde. [onFinished] se llama una sola vez,
 * cuando el overlay ya es transparente.
 */
@Composable
fun BreathSplash(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    onExitStarted: () -> Unit = {}
) {
    val colors = SegundoAireTheme.colors
    val reduced = SegundoAireTheme.motion.reduced
    val description = stringResource(R.string.splash_loading_description)
    val skipLabel = stringResource(R.string.splash_skip)
    val state = remember { BreathSplashState() }
    var skipped by remember { mutableStateOf(false) }
    val currentOnFinished by rememberUpdatedState(onFinished)
    val currentOnExitStarted by rememberUpdatedState(onExitStarted)
    val formRadiusPx = with(LocalDensity.current) { BreathSplashSpec.FormRadius.toPx() }

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            // La fase 6 funde todo el overlay; se lee al dibujar
            .graphicsLayer { alpha = state.overlayAlpha.value }
            .background(colors.background)
            // El overlay se lleva los toques: nada de lo que hay debajo responde mientras tanto
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !skipped,
                onClickLabel = skipLabel,
                role = Role.Button,
                onClick = { skipped = true }
            )
            .clearAndSetSemantics { contentDescription = description }
    ) {
        val releaseTarget = BreathSplashSpec.releaseTargetScale(
            constraints.maxWidth.toFloat(),
            constraints.maxHeight.toFloat(),
            formRadiusPx
        )

        LaunchedEffect(reduced, releaseTarget, skipped) {
            // Un toque cambia `skipped`: la secuencia se cancela y se reanuda en la fase 6
            if (!skipped) {
                if (reduced) state.playReduced() else state.playFull(releaseTarget)
            }

            // 6 · Ingreso: el fundido y la entrada de la pantalla de abajo van a la vez
            currentOnExitStarted()
            if (reduced) {
                state.overlayAlpha.animateTo(0f, BreathSplashSpec.reducedFade())
            } else {
                state.overlayAlpha.animateTo(0f, BreathSplashSpec.exit())
                delay(BreathSplashSpec.ExitTailMillis.toLong())
            }
            currentOnFinished()
        }

        BreathSplashContent(
            formScale = { state.formScale.value },
            formAlpha = { state.formAlpha.value },
            solidAlpha = { state.solidAlpha.value },
            titleProgress = { state.titleProgress.value },
            animated = !reduced,
            titleRise = if (reduced) 0.dp else BreathSplashSpec.TitleRise
        )
    }
}

/**
 * Lo que se ve, sin estado propio: cada valor llega en una lambda que se lee al
 * dibujar. Así las previews fijan un progreso por fase y la app no recompone por cuadro.
 */
@Composable
internal fun BreathSplashContent(
    formScale: () -> Float,
    formAlpha: () -> Float,
    solidAlpha: () -> Float,
    titleProgress: () -> Float,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    titleRise: Dp = BreathSplashSpec.TitleRise
) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val risePx = with(LocalDensity.current) { titleRise.toPx() }

    Box(modifier.fillMaxSize()) {
        // 1 a 4 · La forma que aparece, respira y se expande
        OrganicBlob(
            scale = formScale,
            alpha = formAlpha,
            animated = animated,
            modifier = Modifier.fillMaxSize()
        )

        // Fin de la 4 · Relleno sólido de primary: todo el fondo queda de un solo tono
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = solidAlpha() }
                .background(colors.primary)
        )

        // 5 · Título y eyebrow sobre el primary
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SegundoAireTheme.spacing.space3)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = typography.headlineLg.copy(fontWeight = FontWeight.W500),
                color = colors.onPrimary,
                modifier = Modifier.graphicsLayer {
                    val progress = titleProgress()
                    alpha = progress
                    translationY = (1f - progress) * risePx
                }
            )
            Text(
                text = stringResource(R.string.splash_eyebrow).uppercase(),
                style = typography.labelSm.copy(letterSpacing = 0.08.em),
                color = colors.onPrimary.copy(alpha = BreathSplashSpec.EyebrowAlpha),
                modifier = Modifier.graphicsLayer {
                    // Entra un poco después que el título
                    val lag = BreathSplashSpec.EyebrowLag
                    val progress = ((titleProgress() - lag) / (1f - lag)).coerceIn(0f, 1f)
                    alpha = progress
                    translationY = (1f - progress) * risePx
                }
            )
        }
    }
}
