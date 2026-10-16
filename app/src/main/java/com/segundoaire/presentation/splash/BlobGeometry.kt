package com.segundoaire.presentation.splash

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Geometría pura de la forma orgánica de El Respiro, sin Compose ni Android para que
 * se pueda probar en la JVM. Escribe en arreglos del llamador: se invoca en cada
 * cuadro y no crea objetos.
 *
 * Cada capa son [PointCount] puntos sobre un círculo cuyo radio oscila:
 * `radio_i = base + base * Amplitud * sin(fase * k_i + offset_i)`. Los `k_i` son
 * enteros, así que con la fase de 0 a 2π el movimiento cierra sin salto. Las dos
 * capas usan frecuencias y offsets distintos para que no se muevan al unísono.
 */
object BlobGeometry {
    const val PointCount = 8
    const val LayerCount = 2

    /** Oscilación máxima del radio, como fracción del radio base (±5.5 %). */
    const val AmplitudeFraction = 0.055f

    /** Cada segmento cúbico son dos puntos de control y el punto final: seis números. */
    const val FloatsPerSegment = 6

    const val TwoPi = (2 * PI).toFloat()

    private val Frequencies = arrayOf(
        intArrayOf(1, 2, 3, 2, 1, 3, 2, 3),
        intArrayOf(2, 3, 1, 3, 2, 1, 3, 1)
    )
    private val Offsets = arrayOf(
        floatArrayOf(0f, 1.3f, 2.6f, 3.9f, 5.2f, 0.7f, 2.0f, 4.4f),
        floatArrayOf(2.2f, 0.4f, 3.3f, 5.5f, 1.1f, 4.6f, 0.9f, 3.0f)
    )

    fun radius(baseRadius: Float, phase: Float, index: Int, layer: Int): Float =
        baseRadius + baseRadius * AmplitudeFraction * sin(phase * Frequencies[layer][index] + Offsets[layer][index])

    /** Calcula los [PointCount] puntos de [layer] alrededor de ([centerX], [centerY]); el primero queda arriba. */
    fun points(
        centerX: Float,
        centerY: Float,
        baseRadius: Float,
        phase: Float,
        layer: Int,
        xs: FloatArray,
        ys: FloatArray
    ) {
        for (i in 0 until PointCount) {
            val angle = -PI.toFloat() / 2 + TwoPi * i / PointCount
            val r = radius(baseRadius, phase, i, layer)
            xs[i] = centerX + r * cos(angle)
            ys[i] = centerY + r * sin(angle)
        }
    }

    /**
     * Convierte los puntos en curvas cúbicas con Catmull-Rom uniforme. El segmento
     * `i` va del punto `i` al `i + 1` (el último vuelve al primero, así que la curva
     * cierra) y ocupa `out[i * 6 .. i * 6 + 5]` como `c1x, c1y, c2x, c2y, x, y`.
     */
    fun bezierSegments(xs: FloatArray, ys: FloatArray, out: FloatArray) {
        for (i in 0 until PointCount) {
            val p0 = (i + PointCount - 1) % PointCount
            val p1 = i
            val p2 = (i + 1) % PointCount
            val p3 = (i + 2) % PointCount
            val o = i * FloatsPerSegment
            out[o] = xs[p1] + (xs[p2] - xs[p0]) / 6f
            out[o + 1] = ys[p1] + (ys[p2] - ys[p0]) / 6f
            out[o + 2] = xs[p2] - (xs[p3] - xs[p1]) / 6f
            out[o + 3] = ys[p2] - (ys[p3] - ys[p1]) / 6f
            out[o + 4] = xs[p2]
            out[o + 5] = ys[p2]
        }
    }
}
