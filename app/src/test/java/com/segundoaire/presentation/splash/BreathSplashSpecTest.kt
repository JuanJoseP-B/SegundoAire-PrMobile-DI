package com.segundoaire.presentation.splash

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

class BreathSplashSpecTest {

    private val spec = BreathSplashSpec

    @Test
    fun `la secuencia completa dura 16 s`() {
        assertEquals(16_000L, spec.totalMillis)
    }

    @Test
    fun `cada fase empieza donde la tabla dice`() {
        var t = 0L
        t += spec.PresenceMillis
        assertEquals("respiro", 1_200L, t)
        t += spec.InhaleMillis + spec.ExhaleMillis
        assertEquals("bocanada", 5_200L, t)
        t += spec.DeepMillis + spec.DeepHoldMillis
        assertEquals("liberacion", 10_000L, t)
        t += spec.ReleaseMillis
        assertEquals("revelacion", 11_600L, t)
        t += spec.RevealMillis + spec.RevealHoldMillis
        assertEquals("ingreso", 15_000L, t)
        t += spec.ExitMillis + spec.ExitTailMillis
        assertEquals("fin", 16_000L, t)
    }

    @Test
    fun `con reducir movimiento dura 1_5 s`() {
        assertEquals(1_500L, spec.reducedTotalMillis)
    }

    @Test
    fun `el respiro sube de 1 a 1_14 y la bocanada a 1_6`() {
        assertEquals(1.14f, spec.BreathScale, 0f)
        assertEquals(1.6f, spec.DeepScale, 0f)
        assertTrue(spec.DeepMillis > spec.InhaleMillis)
    }

    @Test
    fun `la forma arranca en 0_55 y las capas suman las opacidades pedidas`() {
        assertEquals(0.55f, spec.PresenceStartScale, 0f)
        assertEquals(0.55f, spec.LayerAAlpha, 0f)
        assertEquals(0.45f, spec.LayerBAlpha, 0f)
        assertEquals(0.22f, spec.HaloAlpha, 0f)
    }

    @Test
    fun `las capas tienen periodos distintos de 7 s y 5_5 s`() {
        assertEquals(7_000, spec.LayerAPeriodMillis)
        assertEquals(5_500, spec.LayerBPeriodMillis)
    }

    @Test
    fun `la rotacion de la segunda capa va de 40 a 70 grados`() {
        assertEquals(40f, spec.RotationFromDegrees, 0f)
        assertEquals(70f, spec.RotationToDegrees, 0f)
    }

    @Test
    fun `el relleno solido entra dentro de la expansion y termina con ella`() {
        assertTrue(spec.SolidFadeDelayMillis + spec.SolidFadeMillis <= spec.ReleaseMillis)
    }

    @Test
    fun `la liberacion cubre la diagonal con margen aun con el borde mas hundido`() {
        val width = 1080f
        val height = 2400f
        val formRadius = 75f * 3f

        val target = spec.releaseTargetScale(width, height, formRadius)
        val coveredRadius = formRadius * target

        assertEquals(hypot(width, height) * 1.15f, coveredRadius, 0.5f)
        // El radio del borde baja hasta 5.5 %; la esquina esta a media diagonal del centro
        assertTrue(coveredRadius * (1f - BlobGeometry.AmplitudeFraction) > hypot(width, height) / 2f)
    }

    @Test
    fun `la escala de liberacion depende de la pantalla y no es un numero fijo`() {
        val phone = spec.releaseTargetScale(1080f, 2400f, 225f)
        val tablet = spec.releaseTargetScale(1600f, 2560f, 225f)

        assertTrue(tablet > phone)
    }

    @Test
    fun `sin radio la liberacion no divide entre cero`() {
        assertEquals(1f, spec.releaseTargetScale(0f, 0f, 0f), 0f)
    }
}
