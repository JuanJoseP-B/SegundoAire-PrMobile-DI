package com.segundoaire.presentation.overlay

import androidx.compose.animation.core.Easing
import androidx.compose.ui.geometry.Offset
import com.segundoaire.presentation.designsystem.theme.Motion

/** Dónde está la hoja del GlassWall: desplazamiento desde su lugar, giro, opacidad y escala. */
internal data class WallLeafPose(val offset: Offset, val rotation: Float, val alpha: Float, val scale: Float = 1f)

/** En qué momento va el GlassWall (los nombres de `openWall` / `closeWall` de la referencia). */
enum class WallStep { Enter, Breath, Ask, Rise, Leave }

/**
 * Tiempos y curvas del GlassWall, sin dibujo, aparte para poder probarlos. Copian las
 * keyframes de la referencia: en CSS cada tramo entre dos keyframes lleva su curva, así
 * que aquí cada pose recibe el tiempo lineal (0–1) y aplica la curva tramo a tramo.
 */
internal object GlassWallChoreography {
    /** La respiración empieza a los 1,7 s y dura 8 s. */
    const val BreathStartMillis = 1700L

    /** A los 9,7 s sube el bloque de contexto con los botones. */
    const val AskAtMillis = 9700L

    /** "Continuar" se habilita tras 10 s de la pregunta (Mindful). */
    const val CountdownSeconds = 10

    /** Copa en reposo, en la respiración llena y con la pregunta a la vista. */
    const val CanopyRest = 0.62f
    const val CanopyFull = 1f
    const val CanopyAsk = 0.8f

    /** En qué paso va el muro [elapsedMillis] después de quedar a la vista. */
    fun stepAt(elapsedMillis: Long): WallStep = when {
        elapsedMillis < BreathStartMillis -> WallStep.Enter
        elapsedMillis < AskAtMillis -> WallStep.Breath
        else -> WallStep.Ask
    }

    /** Segundos que faltan para poder continuar, [elapsedMillis] después de la pregunta. */
    fun countdown(elapsedMillis: Long): Int =
        (CountdownSeconds - (elapsedMillis.coerceAtLeast(0L) / 1000L).toInt()).coerceIn(0, CountdownSeconds)

    /** Avance (0–1) del tallo de "Continuar" según los segundos que faltan. */
    fun stemProgress(secondsLeft: Int): Float =
        (CountdownSeconds - secondsLeft.coerceIn(0, CountdownSeconds)) / CountdownSeconds.toFloat()

    /** Escala de la copa a lo largo de la respiración: 0.62 → 1 → 0.62. */
    fun breathScale(t: Float): Float = segment(
        t, floatArrayOf(0f, 0.5f, 1f), floatArrayOf(CanopyRest, CanopyFull, CanopyRest), Motion.WallBreathEasing
    )

    /** "Inhala" se ve en la primera mitad y se funde entre el 45 % y el 50 %. */
    fun inhaleAlpha(t: Float): Float = segment(t, floatArrayOf(0f, 0.45f, 0.5f, 1f), floatArrayOf(1f, 1f, 0f, 0f), Motion.WallBreathEasing)

    /** "Exhala" aparece entre el 45 % y el 50 % y se va en el último 5 %. */
    fun exhaleAlpha(t: Float): Float =
        segment(t, floatArrayOf(0f, 0.45f, 0.5f, 0.95f, 1f), floatArrayOf(0f, 0f, 1f, 1f, 0f), Motion.WallBreathEasing)

    /**
     * La hoja que cae (1,6 s): de (−60, −260) girada −40° a su lugar girada 20°, pasando por
     * (30, −80) y 80° al 60 %; aparece en el primer 20 %.
     */
    fun leafFall(t: Float): WallLeafPose {
        val keys = floatArrayOf(0f, 0.6f, 1f)
        val x = segment(t, keys, floatArrayOf(-60f, 30f, 0f), Motion.WallFallEasing)
        val y = segment(t, keys, floatArrayOf(-260f, -80f, 0f), Motion.WallFallEasing)
        val rotation = segment(t, keys, floatArrayOf(-40f, 80f, 20f), Motion.WallFallEasing)
        val alpha = segment(t, floatArrayOf(0f, 0.2f, 1f), floatArrayOf(0f, 1f, 1f), Motion.WallFallEasing)
        return WallLeafPose(Offset(x, y), rotation, alpha)
    }

    /** La hoja que sube y se va (1 s): a (40, −420), girada −30°, al 40 % y transparente. */
    fun leafRise(t: Float): WallLeafPose {
        val f = Motion.WallRiseEasing.transform(t.coerceIn(0f, 1f))
        return WallLeafPose(
            offset = Offset(lerp(0f, 40f, f), lerp(0f, -420f, f)),
            rotation = lerp(20f, -30f, f),
            alpha = 1f - f,
            scale = lerp(1f, 0.4f, f)
        )
    }

    /** La hoja ya en su lugar: entre la caída y la subida. */
    val LeafRest = WallLeafPose(Offset.Zero, rotation = 20f, alpha = 1f)

    // Interpola por keyframes; la curva se aplica dentro de cada tramo
    private fun segment(t: Float, keys: FloatArray, values: FloatArray, easing: Easing): Float {
        val time = t.coerceIn(0f, 1f)
        var i = 0
        while (i < keys.lastIndex - 1 && time > keys[i + 1]) i++
        val span = keys[i + 1] - keys[i]
        val local = if (span <= 0f) 1f else ((time - keys[i]) / span).coerceIn(0f, 1f)
        return lerp(values[i], values[i + 1], easing.transform(local))
    }

    private fun lerp(a: Float, b: Float, f: Float): Float = a + (b - a) * f
}
