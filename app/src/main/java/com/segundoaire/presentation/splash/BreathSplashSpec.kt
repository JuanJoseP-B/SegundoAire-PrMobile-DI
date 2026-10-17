package com.segundoaire.presentation.splash

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.dp
import com.segundoaire.presentation.designsystem.theme.Motion
import kotlin.math.hypot

/**
 * Tiempos, curvas y medidas de "El Respiro". Para ajustar la animación se cambia solo
 * este objeto: la coreografía y el dibujo no tienen números propios.
 *
 * Es la única excepción del proyecto a "todo movimiento sale de `Motion`": las curvas
 * `CubicBezier` de la bocanada y la liberación son propias de esta pantalla. Los
 * `tween` viven aquí y en ningún otro archivo del splash.
 *
 * Secuencia, 16 s en total:
 *
 *  1. Presencia:   0.0 a 1.2 s   (1.2 s)
 *  2. Respiro:     1.2 a 5.2 s   (4.0 s)
 *  3. Bocanada:    5.2 a 10.0 s  (4.8 s: 4.0 s de subida y 0.8 s sostenida)
 *  4. Liberación:  10.0 a 11.6 s (1.6 s)
 *  5. Revelación:  11.6 a 15.0 s (3.4 s: 0.9 s de entrada y 2.5 s sostenida)
 *  6. Ingreso:     15.0 a 16.0 s (1.0 s: 0.8 s de fundido y 0.2 s de cola)
 */
object BreathSplashSpec {

    // ---- Forma ----

    /** Radio base: la forma mide unos 150 dp de diámetro. */
    val FormRadius = 75.dp
    const val LayerAPeriodMillis = 7000
    const val LayerBPeriodMillis = 5500

    /** La segunda capa rota despacio entre estos dos ángulos, de ida y vuelta. */
    const val RotationFromDegrees = 40f
    const val RotationToDegrees = 70f
    const val RotationMillis = 12000

    // Opacidad de cada capa y del halo
    const val LayerAAlpha = 0.55f
    const val LayerBAlpha = 0.45f
    const val HaloAlpha = 0.22f
    const val HaloRadiusFactor = 1.9f

    // ---- 1 · Presencia ----

    const val PresenceMillis = 1200
    const val PresenceStartScale = 0.55f
    private val PresenceEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    // ---- 2 · Respiro normal ----

    const val InhaleMillis = 2000
    const val ExhaleMillis = 2000
    const val BreathScale = 1.14f

    // ---- 3 · Bocanada profunda ----

    const val DeepMillis = 4000
    const val DeepScale = 1.6f
    const val DeepHoldMillis = 800
    private val DeepEasing = CubicBezierEasing(0.45f, 0f, 0.35f, 1f)

    // ---- 4 · Liberación ----

    const val ReleaseMillis = 1600

    /** Margen sobre la diagonal para que el borde, que oscila, cubra también las esquinas. */
    const val ReleaseMargin = 1.15f

    /** El relleno sólido de `primary` entra al final de la expansión. */
    const val SolidFadeDelayMillis = 1200
    const val SolidFadeMillis = 400
    private val ReleaseEasing = CubicBezierEasing(0.55f, 0f, 0.2f, 1f)

    // ---- 5 · Revelación ----

    const val RevealMillis = 900
    const val RevealHoldMillis = 2500
    val TitleRise = 8.dp

    /** El eyebrow arranca cuando el título lleva esta fracción de su entrada. */
    const val EyebrowLag = 0.3f
    const val EyebrowAlpha = 0.85f

    // ---- 6 · Ingreso ----

    const val ExitMillis = 800

    /** Lo que falta para completar el segundo de ingreso: las tarjetas terminan de subir. */
    const val ExitTailMillis = 200

    /** Espera entre una tarjeta de la pantalla de bienvenida y la siguiente al aparecer. */
    const val EntranceStaggerMillis = 120

    // ---- Reducir movimiento: forma quieta, título y bienvenida, 1.5 s ----

    const val ReducedShapeHoldMillis = 450
    const val ReducedTitleHoldMillis = 600

    fun <T> presence(): FiniteAnimationSpec<T> = tween(PresenceMillis, easing = PresenceEasing)

    fun <T> inhale(): FiniteAnimationSpec<T> = tween(InhaleMillis, easing = FastOutSlowInEasing)

    fun <T> exhale(): FiniteAnimationSpec<T> = tween(ExhaleMillis, easing = FastOutSlowInEasing)

    fun <T> deep(): FiniteAnimationSpec<T> = tween(DeepMillis, easing = DeepEasing)

    fun <T> release(): FiniteAnimationSpec<T> = tween(ReleaseMillis, easing = ReleaseEasing)

    fun <T> solidFade(): FiniteAnimationSpec<T> = tween(SolidFadeMillis, easing = LinearOutSlowInEasing)

    fun <T> reveal(): FiniteAnimationSpec<T> = tween(RevealMillis, easing = LinearOutSlowInEasing)

    fun <T> exit(): FiniteAnimationSpec<T> = tween(ExitMillis, easing = LinearOutSlowInEasing)

    /** Fase del borde de una capa: una vuelta completa en [periodMillis], sin salto al reiniciar. */
    fun phase(periodMillis: Int): InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(periodMillis, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    )

    fun rotation(): InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(RotationMillis, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
    )

    /** Con reducir movimiento todo cambio es un fundido de 150 ms. */
    fun <T> reducedFade(): FiniteAnimationSpec<T> = Motion.reducedFade()

    /** Escala a la que la forma cubre el área completa, con [ReleaseMargin] sobre la diagonal. */
    fun releaseTargetScale(width: Float, height: Float, formRadiusPx: Float): Float =
        if (formRadiusPx > 0f) hypot(width, height) / formRadiusPx * ReleaseMargin else 1f

    /** Duración de la secuencia completa. */
    val totalMillis: Long
        get() = (
            PresenceMillis + InhaleMillis + ExhaleMillis + DeepMillis + DeepHoldMillis +
                ReleaseMillis + RevealMillis + RevealHoldMillis + ExitMillis + ExitTailMillis
            ).toLong()

    /** Duración con reducir movimiento: forma, pausa, título, pausa y salida. */
    val reducedTotalMillis: Long
        get() = (3 * Motion.ReducedFadeMillis + ReducedShapeHoldMillis + ReducedTitleHoldMillis).toLong()
}
