package com.segundoaire.presentation.overlay

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.util.lerp
import com.segundoaire.R
import com.segundoaire.domain.model.GlassWallState
import com.segundoaire.domain.model.Goal
import com.segundoaire.presentation.designsystem.components.BreathingCircle
import com.segundoaire.presentation.designsystem.components.PrimaryButton
import com.segundoaire.presentation.designsystem.components.TextButton
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MillisPerSecond = 1000L

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
 *
 * [state] decide las acciones de la hoja; ver [GlassWallSheet].
 */
@Composable
fun GlassWall(
    phase: GlassWallPhase,
    state: GlassWallState,
    name: String,
    goal: Goal?,
    onExit: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    onBackdropProgress: (Float) -> Unit = {},
    onHidden: () -> Unit = {},
    nowMillis: () -> Long = System::currentTimeMillis
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
                state = state,
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
                    },
                nowMillis = nowMillis
            )
        }
    }
}

/**
 * La hoja del GlassWall, ya en pantalla: respiración, título y la pregunta con el
 * nombre y el objetivo principal del usuario. Para TalkBack es un diálogo.
 *
 * Las acciones dependen de [state]:
 * - [GlassWallState.NoBlock]: "Volver" y "Continuar".
 * - [GlassWallState.Mindful]: "Continuar" arranca deshabilitado, con la cuenta a la vista.
 * - [GlassWallState.Strict]: solo "Volver a mi bloque" y lo que le queda al bloque,
 *   medido con [nowMillis]. Cuando el bloque termina, vuelven las acciones de sin bloque.
 */
@Composable
fun GlassWallSheet(
    state: GlassWallState,
    name: String,
    goal: Goal?,
    onExit: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    nowMillis: () -> Long = System::currentTimeMillis
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
        Column(
            verticalArrangement = Arrangement.spacedBy(spacing.space2),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state) {
                GlassWallState.NoBlock -> NoBlockActions(onExit, onContinue)
                GlassWallState.Mindful -> MindfulActions(onExit, onContinue)
                is GlassWallState.Strict -> StrictActions(state, nowMillis, onExit, onContinue)
            }
        }
    }
}

@Composable
private fun ColumnScope.NoBlockActions(onExit: () -> Unit, onContinue: () -> Unit) {
    ExitButton(text = stringResource(R.string.overlay_exit), onClick = onExit)
    ContinueButton(text = stringResource(R.string.overlay_continue), onClick = onContinue)
}

// La cuenta vive en la composición: corre solo con la hoja a la vista y empieza de nuevo en cada intercepción
@Composable
private fun ColumnScope.MindfulActions(onExit: () -> Unit, onContinue: () -> Unit) {
    var secondsLeft by remember { mutableIntStateOf(GlassWallState.MINDFUL_WAIT_SECONDS) }
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(MillisPerSecond)
            secondsLeft--
        }
    }

    ExitButton(text = stringResource(R.string.overlay_exit), onClick = onExit)
    ContinueButton(
        text = if (secondsLeft > 0) {
            stringResource(R.string.overlay_continue_in, secondsLeft)
        } else {
            stringResource(R.string.overlay_continue)
        },
        onClick = onContinue,
        // TalkBack lee la cuenta en el propio botón, y avisa cuando ya se puede continuar (§10)
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        enabled = secondsLeft == 0
    )
}

@Composable
private fun ColumnScope.StrictActions(
    state: GlassWallState.Strict,
    nowMillis: () -> Long,
    onExit: () -> Unit,
    onContinue: () -> Unit
) {
    var secondsLeft by remember(state) {
        mutableIntStateOf(remainingSeconds(state.blockEndsAt, nowMillis()))
    }
    LaunchedEffect(state) {
        while (secondsLeft > 0) {
            // Despierta justo al cambiar de segundo, para no saltarse ninguno
            val millisLeft = state.blockEndsAt - nowMillis()
            delay((millisLeft - 1).mod(MillisPerSecond) + 1)
            secondsLeft = remainingSeconds(state.blockEndsAt, nowMillis())
        }
    }
    if (secondsLeft <= 0) {
        NoBlockActions(onExit, onContinue)
        return
    }

    val typography = SegundoAireTheme.typography
    Text(
        text = stringResource(
            R.string.overlay_block_remaining,
            stringResource(R.string.overlay_time_format, secondsLeft / 60, secondsLeft % 60)
        ),
        // Cifras tabulares: el texto no baila mientras corre el reloj
        style = typography.titleMd.copy(fontFeatureSettings = typography.displayTimer.fontFeatureSettings),
        color = SegundoAireTheme.colors.onBackground,
        textAlign = TextAlign.Center
    )
    ExitButton(text = stringResource(R.string.overlay_back_to_block), onClick = onExit)
}

@Composable
private fun ExitButton(text: String, onClick: () -> Unit) {
    PrimaryButton(text = text, onClick = onClick, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun ContinueButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        text = text,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        // Sobre vidrio, primary no cumple contraste como texto (§3.2)
        color = SegundoAireTheme.colors.onBackground,
        enabled = enabled
    )
}

// Hacia arriba: mientras quede algo del último segundo, el bloque sigue en curso
private fun remainingSeconds(blockEndsAt: Long, now: Long): Int =
    ((blockEndsAt - now + MillisPerSecond - 1) / MillisPerSecond).coerceAtLeast(0L).toInt()

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
private fun GlassWallSample(state: GlassWallState) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    Box(
        Modifier
            .background(colors.background)
            .background(colors.scrim)
            .padding(horizontal = spacing.space4, vertical = spacing.space12)
    ) {
        GlassWallSheet(
            state = state,
            name = "",
            goal = Goal.SLEEP,
            onExit = {},
            onContinue = {},
            modifier = Modifier.fillMaxWidth(),
            nowMillis = { 0L }
        )
    }
}

// Con el reloj de la preview en 0: un bloque Strict al que le quedan 12:34
private val PreviewStrict = GlassWallState.Strict(blockEndsAt = 754_000L)

@Preview(name = "GlassWall · sin bloque · claro")
@Composable
private fun GlassWallNoBlockLightPreview() {
    SegundoAireTheme(darkTheme = false) { GlassWallSample(GlassWallState.NoBlock) }
}

@Preview(name = "GlassWall · sin bloque · oscuro")
@Composable
private fun GlassWallNoBlockDarkPreview() {
    SegundoAireTheme(darkTheme = true) { GlassWallSample(GlassWallState.NoBlock) }
}

@Preview(name = "GlassWall · Mindful · claro")
@Composable
private fun GlassWallMindfulLightPreview() {
    SegundoAireTheme(darkTheme = false) { GlassWallSample(GlassWallState.Mindful) }
}

@Preview(name = "GlassWall · Mindful · oscuro")
@Composable
private fun GlassWallMindfulDarkPreview() {
    SegundoAireTheme(darkTheme = true) { GlassWallSample(GlassWallState.Mindful) }
}

@Preview(name = "GlassWall · Strict · claro")
@Composable
private fun GlassWallStrictLightPreview() {
    SegundoAireTheme(darkTheme = false) { GlassWallSample(PreviewStrict) }
}

@Preview(name = "GlassWall · Strict · oscuro")
@Composable
private fun GlassWallStrictDarkPreview() {
    SegundoAireTheme(darkTheme = true) { GlassWallSample(PreviewStrict) }
}
