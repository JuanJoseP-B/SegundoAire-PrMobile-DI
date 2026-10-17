package com.segundoaire.presentation.splash

import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.VectorConverter
import com.segundoaire.domain.model.StartDestination
import com.segundoaire.presentation.designsystem.theme.Motion

/** Qué tanto del splash se muestra. */
enum class SplashVariant {
    /** Primera ejecución o tras reiniciar: respiraciones, bocanada, liberación, texto y salida. */
    Full,

    /** Onboarding ya completado: solo liberación, texto y salida. */
    Short,

    /** Reducir movimiento: sin blob, solo cubrimiento por fundido, texto y salida. */
    Reduced
}

/** Duraciones de cada variante de DESIGN_SYSTEM.md §9, sumadas desde los tokens `Motion`. */
object SplashTimeline {

    fun variantFor(destination: StartDestination, reducedMotion: Boolean): SplashVariant = when {
        reducedMotion -> SplashVariant.Reduced
        destination == StartDestination.ONBOARDING -> SplashVariant.Full
        else -> SplashVariant.Short
    }

    /**
     * Lo que tarda el spring de `splashBreath` en asentarse entre 1.0 y la escala de
     * respiración. Cada tramo dura [Motion.SplashBreathLegMillis]: el spring y, hasta
     * completarlo, una pausa.
     */
    fun breathSpringMillis(): Long = Motion.splashBreath<Float>()
        .vectorize(Float.VectorConverter)
        .getDurationNanos(
            initialValue = AnimationVector1D(1f),
            targetValue = AnimationVector1D(Motion.SplashBreathScale),
            initialVelocity = AnimationVector1D(0f)
        ) / NanosPerMilli

    /** Pausa que completa el tramo de respiración después del spring. */
    fun breathPauseMillis(): Long = (Motion.SplashBreathLegMillis - breathSpringMillis()).coerceAtLeast(0L)

    fun totalMillis(variant: SplashVariant): Long = when (variant) {
        SplashVariant.Full ->
            // Inhalar y exhalar por cada respiración
            Motion.SplashBreathCount * 2L * Motion.SplashBreathLegMillis +
                Motion.SplashDeepMillis + Motion.SplashTensionMillis +
                Motion.SplashReleaseMillis + Motion.SplashTextMillis +
                Motion.SplashHoldMillis + Motion.SplashExitMillis
        SplashVariant.Short ->
            // Sin pausa final: el texto entra y la salida empieza enseguida
            (Motion.SplashReleaseMillis + Motion.SplashTextMillis + Motion.SplashExitMillis).toLong()
        SplashVariant.Reduced ->
            // Cubrimiento, texto y salida de 150 ms, con la pausa de 750 ms entre texto y salida
            (3 * Motion.ReducedFadeMillis + Motion.SplashReducedHoldMillis).toLong()
    }

    private const val NanosPerMilli = 1_000_000L
}
