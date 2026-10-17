package com.segundoaire.presentation.designsystem.theme

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
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

    // ---- Onboarding "Lo que crece cuando pausas" (DESIGN_SYSTEM.md §8.1) ----

    private val GrowEasing = CubicBezierEasing(0.45f, 0f, 0.2f, 1f)
    private val BloomEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
    private val EaseInEasing = CubicBezierEasing(0.42f, 0f, 1f, 1f)
    private val EaseInOutEasing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
    private val EaseEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
    private val EaseOutEasing = CubicBezierEasing(0f, 0f, 0.58f, 1f)
    private val SettleEasing = CubicBezierEasing(0.3f, 1.06f, 0.5f, 1f)
    private val RiseEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    private val CurtainEasing = CubicBezierEasing(0.7f, 0f, 0.2f, 1f)
    private val CountEasing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
    private val RippleEasing = CubicBezierEasing(0.2f, 0.6f, 0.3f, 1f)

    /** Curva de cada tramo de la caída de hojas; la usa `fallPose`, que interpola por keyframes. */
    val GustEasing = CubicBezierEasing(0.3f, 0.1f, 0.4f, 1f)

    /** Curva de cada tramo de la sacudida; la usa `shakeOffset`. */
    val ShakeEasing = CubicBezierEasing(0.36f, 0.07f, 0.19f, 0.97f)
    private val DotEasing = CubicBezierEasing(0.34f, 1.3f, 0.64f, 1f)

    const val GrowMillis = 900
    const val GrowExitMillis = 350
    const val SwayMillis = 7000
    const val SwayDegrees = 0.8f
    const val CurtainMillis = 900
    const val SettleMillis = 1000
    const val LayerMillis = 620
    const val LayerFadeMillis = 380
    const val LayerFadeDelayMillis = 180
    const val ShakeMillis = 380
    const val SpinMillis = 900
    const val RippleMillis = 4000

    /** Trazo que se dibuja: ramas, raíces, anillos y la enredadera. Sale más rápido de lo que entra. */
    fun <T> grow(appear: Boolean = true, delayMillis: Int = 0, durationMillis: Int = GrowMillis): FiniteAnimationSpec<T> =
        if (appear) {
            tween(durationMillis = durationMillis, delayMillis = delayMillis, easing = GrowEasing)
        } else {
            tween(durationMillis = GrowExitMillis, easing = GrowEasing)
        }

    /** Brota con rebote suave (follaje, hojas, flores, semilla); al salir se encoge sin rebote. */
    fun <T> bloom(
        appear: Boolean,
        durationMillis: Int,
        exitMillis: Int = durationMillis,
        delayMillis: Int = 0
    ): FiniteAnimationSpec<T> =
        if (appear) {
            tween(durationMillis = durationMillis, delayMillis = delayMillis, easing = BloomEasing)
        } else {
            tween(durationMillis = exitMillis, delayMillis = delayMillis, easing = EaseInEasing)
        }

    /** Balanceo del árbol: ±[SwayDegrees]° en 7 s, de ida y vuelta. */
    fun sway(): InfiniteRepeatableSpec<Float> = drift(SwayMillis)

    /** Vaivén lento de ida y vuelta: luz de fondo, luciérnagas y balanceo. */
    fun drift(durationMillis: Int, startOffsetMillis: Int = 0): InfiniteRepeatableSpec<Float> =
        infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = EaseInOutEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(startOffsetMillis)
        )

    /** Onda del suelo en la semilla: crece y se desvanece cada 4 s. */
    fun ripple(startOffsetMillis: Int = 0): InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(durationMillis = RippleMillis, easing = RippleEasing),
        repeatMode = RepeatMode.Restart,
        initialStartOffset = StartOffset(startOffsetMillis)
    )

    /**
     * Hojas y pétalos que caen: tiempo lineal de 0 a 1. La curva de cada tramo la
     * aplica `GustPose`, porque en CSS cada intervalo de keyframes tiene la suya.
     */
    fun <T> gust(durationMillis: Int, delayMillis: Int = 0): FiniteAnimationSpec<T> =
        tween(durationMillis = durationMillis, delayMillis = delayMillis, easing = LinearEasing)

    /** Telón del login y del cierre: baja y se retira. */
    fun <T> curtain(): FiniteAnimationSpec<T> = tween(durationMillis = CurtainMillis, easing = CurtainEasing)

    /** Árbol y colina cambian de lugar con un pequeño sobreimpulso. */
    fun <T> settle(): FiniteAnimationSpec<T> = tween(durationMillis = SettleMillis, easing = SettleEasing)

    /** Desplazamiento de las capas de cada paso. */
    fun <T> layer(): FiniteAnimationSpec<T> = tween(durationMillis = LayerMillis, easing = RiseEasing)

    /** Fundido de las capas: la que entra espera a que la anterior se vaya. */
    fun <T> layerFade(entering: Boolean): FiniteAnimationSpec<T> = tween(
        durationMillis = LayerFadeMillis,
        delayMillis = if (entering) LayerFadeDelayMillis else 0,
        easing = EaseOutEasing
    )

    /** Entrada de texto y filas: fundido con subida corta (revelación, plan, opciones). */
    fun <T> rise(durationMillis: Int = 520, delayMillis: Int = 0): FiniteAnimationSpec<T> =
        tween(durationMillis = durationMillis, delayMillis = delayMillis, easing = RiseEasing)

    /** Cambio de propiedad suave (opacidad, color): la curva "ease" del prototipo. */
    fun <T> fade(durationMillis: Int, delayMillis: Int = 0): FiniteAnimationSpec<T> =
        tween(durationMillis = durationMillis, delayMillis = delayMillis, easing = EaseEasing)

    /** Contador que se detiene suave al llegar a su valor. */
    fun <T> count(durationMillis: Int, delayMillis: Int = 0): FiniteAnimationSpec<T> =
        tween(durationMillis = durationMillis, delayMillis = delayMillis, easing = CountEasing)

    /** Sacudida de una opción no permitida: tiempo lineal de 0 a 1; el vaivén lo da `shakeOffset`. */
    fun <T> shake(): FiniteAnimationSpec<T> = tween(durationMillis = ShakeMillis, easing = LinearEasing)

    /** Ancho de los puntos de la bienvenida. */
    fun <T> dot(): FiniteAnimationSpec<T> = tween(durationMillis = 400, easing = DotEasing)

    /** Una mitad del ciclo de respiración del onboarding (4 s), de ida o de vuelta. */
    fun <T> breathHalf(): FiniteAnimationSpec<T> =
        tween(durationMillis = BreathInhaleMillis, easing = EaseInOutEasing)

    /** Semilla girando dentro del botón de crear cuenta. */
    fun spin(): InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(durationMillis = SpinMillis, easing = LinearEasing),
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

    // Onboarding del árbol: con reducir movimiento todo es un fundido de 150 ms. Los bucles
    // (sway, drift, ripple, spin) no tienen versión reducida: quien los usa no los arranca
    // cuando [reduced] es verdadero.

    fun <T> grow(
        appear: Boolean = true,
        delayMillis: Int = 0,
        durationMillis: Int = Motion.GrowMillis
    ): FiniteAnimationSpec<T> =
        if (reduced) Motion.reducedFade() else Motion.grow(appear, delayMillis, durationMillis)

    fun <T> bloom(
        appear: Boolean,
        durationMillis: Int,
        exitMillis: Int = durationMillis,
        delayMillis: Int = 0
    ): FiniteAnimationSpec<T> =
        if (reduced) Motion.reducedFade() else Motion.bloom(appear, durationMillis, exitMillis, delayMillis)

    fun <T> gust(durationMillis: Int, delayMillis: Int = 0): FiniteAnimationSpec<T> =
        Motion.gust(durationMillis, delayMillis)

    fun <T> curtain(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.curtain()

    fun <T> settle(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.settle()

    fun <T> layer(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.layer()

    fun <T> layerFade(entering: Boolean): FiniteAnimationSpec<T> =
        if (reduced) Motion.reducedFade() else Motion.layerFade(entering)

    fun <T> rise(durationMillis: Int = 520, delayMillis: Int = 0): FiniteAnimationSpec<T> =
        if (reduced) Motion.reducedFade() else Motion.rise(durationMillis, delayMillis)

    fun <T> fade(durationMillis: Int, delayMillis: Int = 0): FiniteAnimationSpec<T> =
        if (reduced) Motion.reducedFade() else Motion.fade(durationMillis, delayMillis)

    fun <T> count(durationMillis: Int, delayMillis: Int = 0): FiniteAnimationSpec<T> =
        if (reduced) Motion.reducedFade() else Motion.count(durationMillis, delayMillis)

    fun <T> shake(): FiniteAnimationSpec<T> = Motion.shake()

    fun <T> dot(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.dot()

    fun <T> breathHalf(): FiniteAnimationSpec<T> = Motion.breathHalf()

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
