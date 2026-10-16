package com.segundoaire.presentation.splash

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.hypot

class BlobGeometryTest {

    private val centerX = 180f
    private val centerY = 320f
    private val base = 100f
    private val phases = (0..62).map { it * 0.1f }

    private fun pointsAt(phase: Float): Pair<FloatArray, FloatArray> {
        val xs = FloatArray(BlobGeometry.PointCount)
        val ys = FloatArray(BlobGeometry.PointCount)
        BlobGeometry.points(centerX, centerY, base, phase, xs, ys)
        return xs to ys
    }

    private fun segmentsAt(phase: Float): Triple<FloatArray, FloatArray, FloatArray> {
        val (xs, ys) = pointsAt(phase)
        val segments = FloatArray(BlobGeometry.PointCount * BlobGeometry.FloatsPerSegment)
        BlobGeometry.bezierSegments(xs, ys, segments)
        return Triple(xs, ys, segments)
    }

    @Test
    fun `el blob tiene siete puntos`() {
        val (xs, ys) = pointsAt(0f)

        assertEquals(7, BlobGeometry.PointCount)
        assertEquals(7, xs.size)
        assertEquals(7, ys.size)
    }

    @Test
    fun `puntos y curvas son siempre finitos`() {
        phases.forEach { phase ->
            val (xs, ys, segments) = segmentsAt(phase)
            assertTrue("punto no finito en fase $phase", (xs + ys).all { it.isFinite() })
            assertTrue("curva no finita en fase $phase", segments.all { it.isFinite() })
        }
    }

    @Test
    fun `el radio de cada punto respeta la amplitud`() {
        val limit = base * BlobGeometry.AmplitudeFraction + Tolerance
        phases.forEach { phase ->
            val (xs, ys) = pointsAt(phase)
            for (i in 0 until BlobGeometry.PointCount) {
                val radius = hypot(xs[i] - centerX, ys[i] - centerY)
                assertTrue("punto $i en fase $phase: radio $radius", abs(radius - base) <= limit)
            }
        }
    }

    @Test
    fun `la amplitud no es cero ni pasa del seis por ciento`() {
        val deviations = phases.flatMap { phase ->
            (0 until BlobGeometry.PointCount).map { abs(BlobGeometry.radius(base, phase, it) - base) }
        }

        assertTrue(deviations.max() > base * 0.03f)
        assertTrue(deviations.max() <= base * 0.06f + Tolerance)
    }

    @Test
    fun `el path cierra - el ultimo segmento termina donde empieza el primero`() {
        phases.forEach { phase ->
            val (xs, ys, segments) = segmentsAt(phase)
            val last = (BlobGeometry.PointCount - 1) * BlobGeometry.FloatsPerSegment

            assertEquals(xs[0], segments[last + 4], Tolerance)
            assertEquals(ys[0], segments[last + 5], Tolerance)
        }
    }

    @Test
    fun `cada segmento termina en el punto siguiente`() {
        val (xs, ys, segments) = segmentsAt(1.7f)

        for (i in 0 until BlobGeometry.PointCount) {
            val next = (i + 1) % BlobGeometry.PointCount
            assertEquals(xs[next], segments[i * BlobGeometry.FloatsPerSegment + 4], Tolerance)
            assertEquals(ys[next], segments[i * BlobGeometry.FloatsPerSegment + 5], Tolerance)
        }
    }

    @Test
    fun `la fase cierra el ciclo sin salto`() {
        val (startX, startY) = pointsAt(0f)
        val (endX, endY) = pointsAt(BlobGeometry.TwoPi)

        for (i in 0 until BlobGeometry.PointCount) {
            assertEquals(startX[i], endX[i], 0.01f)
            assertEquals(startY[i], endY[i], 0.01f)
        }
    }

    @Test
    fun `el radio base es una fraccion del lado menor`() {
        assertEquals(72f, BlobGeometry.baseRadius(360f, 800f), Tolerance)
        assertEquals(72f, BlobGeometry.baseRadius(800f, 360f), Tolerance)
    }

    private companion object {
        const val Tolerance = 0.001f
    }
}
