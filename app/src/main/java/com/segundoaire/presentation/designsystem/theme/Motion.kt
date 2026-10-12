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

    private const val CalmMillis = 500
    private val CalmEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    fun <T> press(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.6f, stiffness = 400f)

    fun <T> toggle(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.5f, stiffness = 400f)

    fun <T> nav(): FiniteAnimationSpec<T> = spring(dampingRatio = 1f, stiffness = 1500f)

    fun <T> calm(): FiniteAnimationSpec<T> = tween(durationMillis = CalmMillis, easing = CalmEasing)

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

    fun <T> press(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.press()

    fun <T> toggle(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.toggle()

    fun <T> nav(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.nav()

    fun <T> calm(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.calm()

    fun <T> timer(totalMillis: Int): FiniteAnimationSpec<T> = Motion.timer(totalMillis)

    fun <T> reveal(): FiniteAnimationSpec<T> = Motion.reveal()

    fun <T> step(): FiniteAnimationSpec<T> = if (reduced) Motion.reducedFade() else Motion.step()

    /** El ciclo no tiene versión reducida: con [reduced] el círculo se queda quieto. */
    fun breath(): InfiniteRepeatableSpec<Float> = Motion.breath()

    fun <T> hold(): FiniteAnimationSpec<T> = Motion.hold()

    override fun equals(other: Any?): Boolean = other is SegundoAireMotion && other.reduced == reduced

    override fun hashCode(): Int = reduced.hashCode()
}

/** `true` si la escala de animaciones del sistema es 0. Se actualiza si el ajuste cambia. */
@Composable
fun rememberReducedMotion(): Boolean {
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
