package com.segundoaire.presentation.splash

import androidx.compose.animation.core.Animatable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Los valores animados de El Respiro y la secuencia que los mueve. Todos se leen al
 * dibujar (en lambdas), nunca al componer, así que animarlos no recompone nada.
 *
 * La secuencia corre en una corrutina: no bloquea el hilo principal y se cancela
 * sola si el composable sale o el usuario salta la animación.
 */
internal class BreathSplashState {
    /** Escala de la forma desde su centro. */
    val formScale = Animatable(BreathSplashSpec.PresenceStartScale)

    /** Opacidad de la forma (halo y capas). */
    val formAlpha = Animatable(0f)

    /** Opacidad de la capa sólida de `primary` que cubre toda la pantalla. */
    val solidAlpha = Animatable(0f)

    /** 0 a 1: entrada del título y, un poco después, del eyebrow. */
    val titleProgress = Animatable(0f)

    /** Opacidad de todo el overlay; baja a 0 en la fase 6. */
    val overlayAlpha = Animatable(1f)

    /**
     * Fases 1 a 5. [releaseTarget] es la escala con la que la forma cubre la pantalla.
     * Termina con el título a la vista y la pausa sostenida cumplida.
     */
    suspend fun playFull(releaseTarget: Float) {
        // 1 · Presencia: aparece desde 0.55 y alfa 0
        formScale.snapTo(BreathSplashSpec.PresenceStartScale)
        formAlpha.snapTo(0f)
        coroutineScope {
            launch { formScale.animateTo(1f, BreathSplashSpec.presence()) }
            launch { formAlpha.animateTo(1f, BreathSplashSpec.presence()) }
        }

        // 2 · Respiro normal: una inhalación y una exhalación
        formScale.animateTo(BreathSplashSpec.BreathScale, BreathSplashSpec.inhale())
        formScale.animateTo(1f, BreathSplashSpec.exhale())

        // 3 · Bocanada profunda: sube lento y se sostiene
        formScale.animateTo(BreathSplashSpec.DeepScale, BreathSplashSpec.deep())
        delay(BreathSplashSpec.DeepHoldMillis.toLong())

        // 4 · Liberación: la forma no se contrae, se expande hasta cubrir la pantalla.
        // Al final de la expansión entra el relleno sólido para que el fondo sea de un tono.
        coroutineScope {
            launch { formScale.animateTo(releaseTarget, BreathSplashSpec.release()) }
            launch {
                delay(BreathSplashSpec.SolidFadeDelayMillis.toLong())
                solidAlpha.animateTo(1f, BreathSplashSpec.solidFade())
            }
        }

        // 5 · Revelación: título, eyebrow y pausa
        titleProgress.animateTo(1f, BreathSplashSpec.reveal())
        delay(BreathSplashSpec.RevealHoldMillis.toLong())
    }

    /** Reducir movimiento: forma quieta, luego cubrimiento con título, todo por fundidos de 150 ms. */
    suspend fun playReduced() {
        formScale.snapTo(1f)
        formAlpha.animateTo(1f, BreathSplashSpec.reducedFade())
        delay(BreathSplashSpec.ReducedShapeHoldMillis.toLong())
        coroutineScope {
            launch { solidAlpha.animateTo(1f, BreathSplashSpec.reducedFade()) }
            launch { titleProgress.animateTo(1f, BreathSplashSpec.reducedFade()) }
        }
        delay(BreathSplashSpec.ReducedTitleHoldMillis.toLong())
    }
}
