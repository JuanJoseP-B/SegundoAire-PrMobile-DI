package com.segundoaire.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GlassWallStateTest {

    private val start = 1_000_000L

    private fun block(rigor: RigorLevel, minutes: Int = 15) = ActiveBlock.starting(start, minutes, rigor)

    @Test
    fun `sin bloque el overlay ofrece volver o continuar`() {
        assertEquals(GlassWallState.NoBlock, GlassWallState.of(block = null, now = start))
    }

    @Test
    fun `un bloque Mindful en curso da la variante Mindful`() {
        val state = GlassWallState.of(block(RigorLevel.MINDFUL), now = start + 60_000L)

        assertEquals(GlassWallState.Mindful, state)
    }

    @Test
    fun `un bloque Strict en curso da la variante Strict con su hora de fin`() {
        val block = block(RigorLevel.STRICT, minutes = 30)

        val state = GlassWallState.of(block, now = start + 60_000L)

        assertEquals(GlassWallState.Strict(blockEndsAt = start + 30 * 60_000L), state)
    }

    @Test
    fun `el rigor aplica desde el primer instante del bloque`() {
        assertEquals(GlassWallState.Mindful, GlassWallState.of(block(RigorLevel.MINDFUL), now = start))
        assertEquals(
            GlassWallState.Strict(start + 15 * 60_000L),
            GlassWallState.of(block(RigorLevel.STRICT), now = start)
        )
    }

    @Test
    fun `Strict se mantiene hasta el ultimo milisegundo del bloque`() {
        val block = block(RigorLevel.STRICT, minutes = 5)

        val state = GlassWallState.of(block, now = block.endsAt - 1)

        assertEquals(GlassWallState.Strict(block.endsAt), state)
    }

    @Test
    fun `un bloque vencido que nadie retiro cuenta como sin bloque`() {
        val strict = block(RigorLevel.STRICT, minutes = 5)
        val mindful = block(RigorLevel.MINDFUL, minutes = 5)

        assertEquals(GlassWallState.NoBlock, GlassWallState.of(strict, now = strict.endsAt))
        assertEquals(GlassWallState.NoBlock, GlassWallState.of(mindful, now = mindful.endsAt + 60_000L))
    }

    @Test
    fun `la espera de Mindful es de diez segundos`() {
        assertEquals(10, GlassWallState.MINDFUL_WAIT_SECONDS)
    }
}
