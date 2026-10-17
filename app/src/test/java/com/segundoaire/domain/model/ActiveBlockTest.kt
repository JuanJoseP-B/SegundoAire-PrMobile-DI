package com.segundoaire.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveBlockTest {

    private val start = 1_000_000L
    private val block = ActiveBlock.starting(now = start, durationMinutes = 5, rigor = RigorLevel.MINDFUL)

    @Test
    fun `el fin es el inicio mas la duracion`() {
        assertEquals(start + 300_000L, block.endsAt)
        assertEquals(300_000L, block.totalMillis)
    }

    @Test
    fun `el restante es el fin menos la hora actual`() {
        assertEquals(300_000L, block.remainingMillis(start))
        assertEquals(299_000L, block.remainingMillis(start + 1_000))
        assertEquals(1L, block.remainingMillis(block.endsAt - 1))
    }

    @Test
    fun `pasado el fin el restante es cero y no negativo`() {
        assertEquals(0L, block.remainingMillis(block.endsAt))
        assertEquals(0L, block.remainingMillis(block.endsAt + 60_000))
    }

    @Test
    fun `si el reloj retrocede el restante no supera la duracion`() {
        assertEquals(300_000L, block.remainingMillis(start - 3_600_000))
    }

    @Test
    fun `esta activo hasta el instante de fin, sin incluirlo`() {
        assertTrue(block.isActiveAt(start))
        assertTrue(block.isActiveAt(block.endsAt - 1))
        assertFalse(block.isActiveAt(block.endsAt))
    }

    @Test
    fun `el avance va de cero al iniciar a uno al terminar`() {
        assertEquals(0f, block.progress(start), 0.0001f)
        assertEquals(0.5f, block.progress(start + 150_000), 0.0001f)
        assertEquals(1f, block.progress(block.endsAt), 0.0001f)
        assertEquals(1f, block.progress(block.endsAt + 60_000), 0.0001f)
    }

    @Test
    fun `los segundos restantes se redondean hacia arriba`() {
        assertEquals(300, block.remainingSeconds(start))
        assertEquals(300, block.remainingSeconds(start + 1))
        assertEquals(1, block.remainingSeconds(block.endsAt - 1))
        assertEquals(0, block.remainingSeconds(block.endsAt))
    }
}
