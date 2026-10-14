package com.segundoaire.presentation.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
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
    onExit: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    onBackdropProgress: (Float) -> Unit = {},
    onHidden: () -> Unit = {}
) {
    val colors = SegundoAireTheme.colors
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
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (sheetVisible) {
            GlassWallSheet(
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

@Composable
private fun GlassWallSheet(
    onExit: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        contentPadding = PaddingValues(SegundoAireTheme.spacing.space6),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.overlay_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.overlay_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(R.string.overlay_exit),
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.overlay_continue),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
