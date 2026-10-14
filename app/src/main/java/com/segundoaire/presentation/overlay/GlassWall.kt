package com.segundoaire.presentation.overlay

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.util.lerp
import com.segundoaire.R
import com.segundoaire.domain.model.Goal
import com.segundoaire.presentation.designsystem.components.BreathingCircle
import com.segundoaire.presentation.designsystem.components.PrimaryButton
import com.segundoaire.presentation.designsystem.components.TextButton
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Momento del GlassWall dentro de su ventana. */
enum class GlassWallPhase {
    /** Recién agregado a la ventana: todavía no se ve nada. */
    Waiting,

    /** Entra el fondo y después la hoja. */
    Visible,

    /** Sale todo a la vez; al terminar avisa para quitar la ventana. */
    Leaving
}

/**
 * El "Muro de Cristal". Sin lógica: anima según [phase] y emite eventos hacia arriba.
 *
 * Entrada `calm`: primero el fondo (el scrim que se dibuja aquí y el blur de la
 * ventana, que quien aloja el overlay aplica con [onBackdropProgress]) y después
 * la hoja, con fundido y escala. La salida es el proceso inverso con `calmExit`
 * y termina en [onHidden]. Con reducir movimiento todo es un fundido de 150 ms.
 */
@Composable
fun GlassWall(
    phase: GlassWallPhase,
    name: String,
    goal: Goal?,
    onExit: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    onBackdropProgress: (Float) -> Unit = {},
    onHidden: () -> Unit = {}
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val motion by rememberUpdatedState(SegundoAireTheme.motion)
    val backdrop = remember { Animatable(0f) }
    val sheet = remember { Animatable(0f) }
    val notifyBackdrop by rememberUpdatedState(onBackdropProgress)
    val notifyHidden by rememberUpdatedState(onHidden)

    LaunchedEffect(phase) {
        when (phase) {
            GlassWallPhase.Waiting -> Unit
            GlassWallPhase.Visible -> {
                // El bloque corre una vez por frame: es el paso máximo del blur de la ventana
                backdrop.animateTo(1f, motion.calm()) { notifyBackdrop(value) }
                sheet.animateTo(1f, motion.calm())
            }
            GlassWallPhase.Leaving -> {
                coroutineScope {
                    launch { sheet.animateTo(0f, motion.calmExit()) }
                    launch { backdrop.animateTo(0f, motion.calmExit()) { notifyBackdrop(value) } }
                }
                notifyHidden()
            }
        }
    }

    // Fuera de la composición mientras no se ve: una hoja transparente seguiría recibiendo toques
    val sheetVisible by remember { derivedStateOf { sheet.value > 0f } }

    Box(
        modifier = modifier
            // Velo bajo el blur de la ventana; en Android 10–11 o sin blur es todo el fondo
            .drawBehind { drawRect(colors.scrim, alpha = backdrop.value) }
            .safeDrawingPadding()
            .padding(horizontal = spacing.space4, vertical = spacing.space12),
        contentAlignment = Alignment.Center
    ) {
        if (sheetVisible) {
            GlassWallSheet(
                name = name,
                goal = goal,
                onExit = onExit,
                onContinue = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        val scale = lerp(motion.calmEnterScale, 1f, sheet.value)
                        alpha = sheet.value
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }
    }
}

/**
 * La hoja del GlassWall, ya en pantalla: respiración, título y la pregunta con el
 * nombre y el objetivo principal del usuario. Para TalkBack es un diálogo.
 */
@Composable
fun GlassWallSheet(
    name: String,
    goal: Goal?,
    onExit: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val typography = SegundoAireTheme.typography
    val title = stringResource(R.string.overlay_title)

    Column(
        modifier = modifier
            .glass(GlassLevel.Level3, SegundoAireTheme.shapes.radiusXl)
            .semantics {
                dialog()
                paneTitle = title
            }
            // En pantallas bajas o con la fuente grande la hoja no cabe entera
            .verticalScroll(rememberScrollState())
            .padding(spacing.space6),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BreathingCircle()
        Spacer(Modifier.height(spacing.space6))
        Text(
            text = title,
            style = typography.headlineLg,
            color = colors.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(spacing.space3))
        Text(
            text = glassWallMessage(name, goal),
            style = typography.bodyLg,
            color = colors.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(spacing.space8))
        PrimaryButton(
            text = stringResource(R.string.overlay_exit),
            onClick = onExit,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(spacing.space2))
        TextButton(
            text = stringResource(R.string.overlay_continue),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            // Sobre vidrio, primary no cumple contraste como texto (§3.2)
            color = colors.onBackground
        )
    }
}

/** "Ana, ¿esto te acerca a dormir mejor?"; sin nombre guardado, la misma pregunta sin vocativo. */
@Composable
private fun glassWallMessage(name: String, goal: Goal?): String {
    val trimmed = name.trim()
    return if (trimmed.isEmpty()) {
        stringResource(anonymousMessageRes(goal))
    } else {
        stringResource(messageRes(goal), trimmed)
    }
}

@StringRes
private fun messageRes(goal: Goal?): Int = when (goal) {
    Goal.FOCUS -> R.string.overlay_message_focus
    Goal.SLEEP -> R.string.overlay_message_sleep
    Goal.PRESENCE -> R.string.overlay_message_presence
    Goal.MENTAL_HEALTH -> R.string.overlay_message_mental_health
    Goal.FREE_TIME -> R.string.overlay_message_free_time
    null -> R.string.overlay_message_generic
}

@StringRes
private fun anonymousMessageRes(goal: Goal?): Int = when (goal) {
    Goal.FOCUS -> R.string.overlay_message_focus_anon
    Goal.SLEEP -> R.string.overlay_message_sleep_anon
    Goal.PRESENCE -> R.string.overlay_message_presence_anon
    Goal.MENTAL_HEALTH -> R.string.overlay_message_mental_health_anon
    Goal.FREE_TIME -> R.string.overlay_message_free_time_anon
    null -> R.string.overlay_message_generic_anon
}

// La hoja suelta sobre el velo: las animaciones de entrada no corren en una preview
@Composable
private fun GlassWallSample() {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    Box(
        Modifier
            .background(colors.background)
            .background(colors.scrim)
            .padding(horizontal = spacing.space4, vertical = spacing.space12)
    ) {
        GlassWallSheet(
            name = "",
            goal = Goal.SLEEP,
            onExit = {},
            onContinue = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "GlassWall · claro")
@Composable
private fun GlassWallLightPreview() {
    SegundoAireTheme(darkTheme = false) { GlassWallSample() }
}

@Preview(name = "GlassWall · oscuro")
@Composable
private fun GlassWallDarkPreview() {
    SegundoAireTheme(darkTheme = true) { GlassWallSample() }
}
