package com.segundoaire.presentation.designsystem.theme

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Los nueve tokens de movimiento de DESIGN_SYSTEM.md §8, sin ajuste de
 * accesibilidad. Las pantallas no usan este objeto: piden el movimiento a
 * `SegundoAireTheme.motion`, que ya respeta reducir movimiento.
 */
object Motion {
    const val PressedScale = 0.95f
    const val RevealCharDelayMillis = 18
    const val RevealCharFadeMillis = 150
    val RevealRise = 4.dp
    val StepOffset = 24.dp
    const val BreathInhaleMillis = 4000
    const val BreathExhaleMillis = 4000
    const val HoldMillis = 1500
    const val ReducedFadeMillis = 150

    // La hoja del GlassWall entra creciendo desde esta escala
    const val CalmEnterScale = 0.92f

    private const val CalmMillis = 500
    private const val CalmExitMillis = 250
    private val CalmEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    fun <T> press(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.6f, stiffness = 400f)

    fun <T> toggle(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.5f, stiffness = 400f)

    fun <T> nav(): FiniteAnimationSpec<T> = spring(dampingRatio = 1f, stiffness = 1500f)

    fun <T> calm(): FiniteAnimationSpec<T> = tween(durationMillis = CalmMillis, easing = CalmEasing)

    /** La salida de `calm`: la misma curva en la mitad del tiempo. */
    fun <T> calmExit(): FiniteAnimationSpec<T> = tween(durationMillis = CalmExitMillis, easing = CalmEasing)

    fun <T> timer(totalMillis: Int): FiniteAnimationSpec<T> =
        tween(durationMillis = totalMillis, easing = LinearEasing)

    fun <T> reveal(): FiniteAnimationSpec<T> = tween(durationMillis = RevealCharFadeMillis)

    fun <T> step(): FiniteAnimationSpec<T> = spring(dampingRatio = 1f, stiffness = 400f)

    fun breath(): InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(durationMillis = BreathInhaleMillis, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
    )

    fun <T> hold(): FiniteAnimationSpec<T> = tween(durationMillis = HoldMillis, easing = LinearEasing)

    fun <T> reducedFade(): FiniteAnimationSpec<T> = tween(durationMillis = ReducedFadeMillis)

    // Splash El Respiro, DESIGN_SYSTEM.md §9. Las duraciones son constantes para que
    // la línea de tiempo (SplashTimeline) pueda sumarlas sin animar nada.
    const val SplashBreathCount = 2

    // Cada inhalar o exhalar dura esto: el spring se asienta y el resto es pausa
    const val SplashBreathLegMillis = 1000
    const val SplashBreathScale = 1.2f
    const val SplashDeepScale = 1.4f
    const val SplashDeepMillis = 1000

    // Pausa de tensión entre la bocanada y la liberación
    const val SplashTensionMillis = 150
    const val SplashReleaseMillis = 500
    const val SplashTextMillis = 300
    const val SplashHoldMillis = 1000
    const val SplashExitMillis = 200

    // Reducir movimiento: cubrimiento + texto + pausa + salida suman 1.2 s
    const val SplashReducedHoldMillis = 750

    // Un ciclo de la fase del borde del blob; sus frecuencias son enteras, así que cierra sin salto
    const val SplashPhaseMillis = 6000

    fun <T> splashBreath(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.8f, stiffness = 200f)

    fun <T> splashDeep(): FiniteAnimationSpec<T> =
        tween(durationMillis = SplashDeepMillis, easing = EaseOutCubic)

    fun <T> splashRelease(): FiniteAnimationSpec<T> =
        tween(durationMillis = SplashReleaseMillis, easing = FastOutSlowInEasing)

    fun <T> splashText(): FiniteAnimationSpec<T> =
        tween(durationMillis = SplashTextMillis, easing = LinearEasing)

    fun <T> splashExit(): FiniteAnimationSpec<T> =
        tween(durationMillis = SplashExitMillis, easing = LinearEasing)

    /** Única excepción lineal documentada: la fase del borde mantiene la respiración continua. */
    fun splashPhase(): InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(durationMillis = SplashPhaseMillis, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    )
}

/**
 * Movimiento ya resuelto para el usuario. Con [reduced], todo pasa a un fundido
 * de 150 ms, sin escala ni desplazamiento; `timer` y `hold` no cambian porque
 * representan tiempo real.
 */
@Immutable
class SegundoAireMotion(val reduced: Boolean = false) {
    val pressedScale: Float get() = if (reduced) 1f else Motion.PressedScale
    val revealCharDelayMillis: Int get() = if (reduced) 0 else Motion.RevealCharDelayMillis
    val revealRise: Dp get() = if (reduced) 0.dp else Motion.RevealRise
    val stepOffset: Dp get() = if (reduced) 0.dp else Motion.StepOffset
    val calmEnterScale: Float get() = if (reduced) 1f else Motion.CalmEnterScale

    fun <T> press(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.press()

    fun <T> toggle(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.toggle()

    fun <T> nav(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.nav()

    fun <T> calm(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.calm()

    fun <T> calmExit(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.calmExit()

    fun <T> timer(totalMillis: Int): FiniteAnimationSpec<T> = Motion.timer(totalMillis)

    fun <T> reveal(): FiniteAnimationSpec<T> = Motion.reveal()

    fun <T> step(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.step()

    /** El ciclo no tiene versión reducida: con [reduced] el círculo se queda quieto. */
    fun breath(): InfiniteRepeatableSpec<Float> = Motion.breath()

    fun <T> hold(): FiniteAnimationSpec<T> = Motion.hold()

    // El blob no existe con reducir movimiento: solo quedan el cubrimiento, el texto y la salida,
    // todos como fundido de 150 ms. Las tres curvas del blob se dejan en fundido por si se piden.
    val splashTextMillis: Int get() = if (reduced) Motion.ReducedFadeMillis else Motion.SplashTextMillis
    val splashHoldMillis: Int get() = if (reduced) Motion.SplashReducedHoldMillis else Motion.SplashHoldMillis
    val splashExitMillis: Int get() = if (reduced) Motion.ReducedFadeMillis else Motion.SplashExitMillis

    fun <T> splashBreath(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.splashBreath()

    fun <T> splashDeep(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.splashDeep()

    fun <T> splashRelease(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.splashRelease()

    fun <T> splashText(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.splashText()

    fun <T> splashExit(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.splashExit()

    fun splashPhase(): InfiniteRepeatableSpec<Float> = Motion.splashPhase()

    override fun equals(other: Any?): Boolean = other is SegundoAireMotion && other.reduced == reduced

    override fun hashCode(): Int = reduced.hashCode()
}

/** `true` si la escala de animaciones del sistema es 0. Se actualiza si el ajuste cambia. */
@Composable
fun rememberReducedMotion(): Boolean {
    // Las previews no tienen ajustes del sistema que leer
    if (LocalInspectionMode.current) return false

    val resolver = LocalContext.current.contentResolver
    fun read() = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

    var reduced by remember(resolver) { mutableStateOf(read()) }
    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                reduced = read()
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer
        )
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return reduced
}
