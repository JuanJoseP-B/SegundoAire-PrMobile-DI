package com.segundoaire.presentation.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class GlassWallChoreographyTest {

    @Test
    fun `entra, respira desde 1,7 s y pregunta a los 9,7 s`() {
        assertEquals(WallStep.Enter, GlassWallChoreography.stepAt(0))
        assertEquals(WallStep.Enter, GlassWallChoreography.stepAt(1699))
        assertEquals(WallStep.Breath, GlassWallChoreography.stepAt(1700))
        assertEquals(WallStep.Breath, GlassWallChoreography.stepAt(9699))
        assertEquals(WallStep.Ask, GlassWallChoreography.stepAt(9700))
    }

    @Test
    fun `la cuenta de Mindful baja un segundo por vez y se queda en cero`() {
        assertEquals(10, GlassWallChoreography.countdown(0))
        assertEquals(10, GlassWallChoreography.countdown(999))
        assertEquals(9, GlassWallChoreography.countdown(1000))
        assertEquals(1, GlassWallChoreography.countdown(9999))
        assertEquals(0, GlassWallChoreography.countdown(10_000))
        assertEquals(0, GlassWallChoreography.countdown(60_000))
    }

    @Test
    fun `el tallo de continuar se dibuja con la cuenta`() {
        assertEquals(0f, GlassWallChoreography.stemProgress(10), 0.0001f)
        assertEquals(0.5f, GlassWallChoreography.stemProgress(5), 0.0001f)
        assertEquals(1f, GlassWallChoreography.stemProgress(0), 0.0001f)
    }

    @Test
    fun `la copa respira de 0,62 a 1 y vuelve`() {
        assertEquals(0.62f, GlassWallChoreography.breathScale(0f), 0.0001f)
        assertEquals(1f, GlassWallChoreography.breathScale(0.5f), 0.0001f)
        assertEquals(0.62f, GlassWallChoreography.breathScale(1f), 0.0001f)
    }

    @Test
    fun `inhala en la primera mitad y exhala en la segunda`() {
        assertEquals(1f, GlassWallChoreography.inhaleAlpha(0.2f), 0.0001f)
        assertEquals(0f, GlassWallChoreography.inhaleAlpha(0.7f), 0.0001f)
        assertEquals(0f, GlassWallChoreography.exhaleAlpha(0.2f), 0.0001f)
        assertEquals(1f, GlassWallChoreography.exhaleAlpha(0.7f), 0.0001f)
        assertEquals(0f, GlassWallChoreography.exhaleAlpha(1f), 0.0001f)
    }

    @Test
    fun `la hoja cae desde arriba a su lugar y al subir se va`() {
        val start = GlassWallChoreography.leafFall(0f)
        val end = GlassWallChoreography.leafFall(1f)
        assertEquals(-60f, start.offset.x, 0.001f)
        assertEquals(-260f, start.offset.y, 0.001f)
        assertEquals(0f, start.alpha, 0.001f)
        assertEquals(GlassWallChoreography.LeafRest, end)

        val gone = GlassWallChoreography.leafRise(1f)
        assertEquals(-420f, gone.offset.y, 0.001f)
        assertEquals(0f, gone.alpha, 0.001f)
        assertEquals(0.4f, gone.scale, 0.001f)
    }
}
