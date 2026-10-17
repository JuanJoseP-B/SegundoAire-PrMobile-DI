package com.segundoaire.presentation.splash

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.hypot

class BlobGeometryTest {

    private val centerX = 180f
    private val centerY = 320f
    private val base = 100f
    private val phases = (0..62).map { it * 0.1f }
    private val layers = 0 until BlobGeometry.LayerCount

    private fun pointsAt(phase: Float, layer: Int): Pair<FloatArray, FloatArray> {
        val xs = FloatArray(BlobGeometry.PointCount)
        val ys = FloatArray(BlobGeometry.PointCount)
        BlobGeometry.points(centerX, centerY, base, phase, layer, xs, ys)
        return xs to ys
    }

    private fun segmentsAt(phase: Float, layer: Int): Triple<FloatArray, FloatArray, FloatArray> {
        val (xs, ys) = pointsAt(phase, layer)
        val segments = FloatArray(BlobGeometry.PointCount * BlobGeometry.FloatsPerSegment)
        BlobGeometry.bezierSegments(xs, ys, segments)
        return Triple(xs, ys, segments)
    }

    @Test
    fun `la forma tiene ocho puntos en cada una de sus dos capas`() {
        assertEquals(8, BlobGeometry.PointCount)
        assertEquals(2, BlobGeometry.LayerCount)
        layers.forEach { layer ->
            val (xs, ys) = pointsAt(0f, layer)
            assertEquals(8, xs.size)
            assertEquals(8, ys.size)
        }
    }

    @Test
    fun `puntos y curvas son siempre finitos`() {
        layers.forEach { layer ->
            phases.forEach { phase ->
                val (xs, ys, segments) = segmentsAt(phase, layer)
                assertTrue("punto no finito en capa $layer, fase $phase", (xs + ys).all { it.isFinite() })
                assertTrue("curva no finita en capa $layer, fase $phase", segments.all { it.isFinite() })
            }
        }
    }

    @Test
    fun `el radio de cada punto respeta la amplitud`() {
        val limit = base * BlobGeometry.AmplitudeFraction + Tolerance
        layers.forEach { layer ->
            phases.forEach { phase ->
                val (xs, ys) = pointsAt(phase, layer)
                for (i in 0 until BlobGeometry.PointCount) {
                    val radius = hypot(xs[i] - centerX, ys[i] - centerY)
                    assertTrue("capa $layer, punto $i, fase $phase: radio $radius", abs(radius - base) <= limit)
                }
            }
        }
    }

    @Test
    fun `la amplitud esta entre cinco y seis por ciento`() {
        assertTrue(BlobGeometry.AmplitudeFraction in 0.05f..0.06f)

        val deviations = layers.flatMap { layer ->
            phases.flatMap { phase ->
                (0 until BlobGeometry.PointCount).map { abs(BlobGeometry.radius(base, phase, it, layer) - base) }
            }
        }
        // La oscilacion llega casi a su tope y nunca lo pasa
        assertTrue(deviations.max() > base * 0.04f)
        assertTrue(deviations.max() <= base * 0.06f)
    }

    @Test
    fun `el path cierra - el ultimo segmento termina donde empieza el primero`() {
        layers.forEach { layer ->
            phases.forEach { phase ->
                val (xs, ys, segments) = segmentsAt(phase, layer)
                val last = (BlobGeometry.PointCount - 1) * BlobGeometry.FloatsPerSegment

                assertEquals(xs[0], segments[last + 4], Tolerance)
                assertEquals(ys[0], segments[last + 5], Tolerance)
            }
        }
    }

    @Test
    fun `cada segmento termina en el punto siguiente`() {
        val (xs, ys, segments) = segmentsAt(1.7f, layer = 0)

        for (i in 0 until BlobGeometry.PointCount) {
            val next = (i + 1) % BlobGeometry.PointCount
            assertEquals(xs[next], segments[i * BlobGeometry.FloatsPerSegment + 4], Tolerance)
            assertEquals(ys[next], segments[i * BlobGeometry.FloatsPerSegment + 5], Tolerance)
        }
    }

    @Test
    fun `la fase cierra el ciclo sin salto en las dos capas`() {
        layers.forEach { layer ->
            val (startX, startY) = pointsAt(0f, layer)
            val (endX, endY) = pointsAt(BlobGeometry.TwoPi, layer)

            for (i in 0 until BlobGeometry.PointCount) {
                assertEquals(startX[i], endX[i], 0.01f)
                assertEquals(startY[i], endY[i], 0.01f)
            }
        }
    }

    @Test
    fun `las dos capas no se mueven al unisono`() {
        val (aX, _) = pointsAt(1.3f, layer = 0)
        val (bX, _) = pointsAt(1.3f, layer = 1)

        assertNotEquals(aX.toList(), bX.toList())
    }

    private companion object {
        const val Tolerance = 0.001f
    }
}
