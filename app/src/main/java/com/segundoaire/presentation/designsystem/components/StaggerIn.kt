package com.segundoaire.presentation.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import kotlinx.coroutines.delay

/** Espera entre un elemento y el siguiente de una lista que aparece escalonada. */
const val StaggerStepMillis = 40

/**
 * `true` mientras algo cubre la pantalla (el splash): las entradas escalonadas se
 * quedan ocultas y arrancan en cuanto vuelve a `false`, para que se vean al revelarse.
 */
val LocalEntranceHold = compositionLocalOf { false }

/** Retraso del elemento [index]: sin desplazamiento (reducir movimiento) aparecen todos a la vez. */
internal fun staggerDelayMillis(index: Int, reduced: Boolean, stepMillis: Int = StaggerStepMillis): Int =
    if (reduced) 0 else index.coerceAtLeast(0) * stepMillis

/**
 * Entrada escalonada de una lista (DESIGN_SYSTEM.md §8): fundido y subida con
 * `Motion.nav`, [stepMillis] más tarde por cada [index]. Con reducir movimiento
 * queda un fundido sin subida ni espera. Si [LocalEntranceHold] está activo, espera
 * a que se libere antes de empezar.
 *
 * Solo se aplica en la primera carga: quien llama decide [animate] una vez al
 * componer el elemento, porque cambiarlo después cortaría la animación.
 */
@Composable
fun Modifier.staggerIn(
    animate: Boolean,
    index: Int,
    stepMillis: Int = StaggerStepMillis
): Modifier {
    if (!animate) return this

    val motion = SegundoAireTheme.motion
    val risePx = with(LocalDensity.current) { motion.stepOffset.toPx() }
    val delayMillis = staggerDelayMillis(index, motion.reduced, stepMillis)
    val hold = LocalEntranceHold.current
    val progress = remember { Animatable(0f) }

    LaunchedEffect(hold) {
        if (!hold) {
            delay(delayMillis.toLong())
            progress.animateTo(1f, motion.nav())
        }
    }

    return graphicsLayer {
        // El resorte puede pasarse de 1: la opacidad se acota, la posición no
        alpha = progress.value.coerceIn(0f, 1f)
        translationY = (1f - progress.value) * risePx
    }
}
