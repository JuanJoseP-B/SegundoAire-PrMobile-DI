package com.segundoaire.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import com.segundoaire.presentation.designsystem.theme.Motion
import com.segundoaire.presentation.designsystem.theme.SegundoAireMotion
import kotlinx.coroutines.delay
import kotlin.math.hypot

/** Escala a la que el blob cubre toda el área, con 15 % de margen sobre la diagonal. */
internal fun releaseTargetScale(width: Float, height: Float): Float {
    val baseRadius = BlobGeometry.baseRadius(width, height)
    return if (baseRadius > 0f) hypot(width, height) / baseRadius * ReleaseMargin else 1f
}

private const val ReleaseMargin = 1.15f

/**
 * Coreografía del blob con una sola escala [scale], sin bloquear el hilo principal
 * (corre en una corrutina). La escala se lee al dibujar, nunca al componer.
 *
 * - [SplashVariant.Full]: dos respiraciones 1.0 → 1.2 → 1.0, bocanada hasta 1.4,
 *   pausa de tensión y liberación.
 * - [SplashVariant.Short]: solo la liberación.
 *
 * [onRelease] recibe la escala desde la que arranca la liberación; el centro queda
 * fijo y la escala sube hasta [releaseTarget].
 */
internal suspend fun playBlob(
    scale: Animatable<Float, AnimationVector1D>,
    variant: SplashVariant,
    motion: SegundoAireMotion,
    releaseTarget: Float,
    onRelease: (from: Float) -> Unit
) {
    if (variant == SplashVariant.Full) {
        val pause = SplashTimeline.breathPauseMillis()
        scale.snapTo(1f)
        repeat(Motion.SplashBreathCount) {
            scale.animateTo(Motion.SplashBreathScale, motion.splashBreath())
            delay(pause)
            scale.animateTo(1f, motion.splashBreath())
            delay(pause)
        }
        scale.animateTo(Motion.SplashDeepScale, motion.splashDeep())
        delay(Motion.SplashTensionMillis.toLong())
    }
    onRelease(scale.value)
    scale.animateTo(releaseTarget, motion.splashRelease())
}
