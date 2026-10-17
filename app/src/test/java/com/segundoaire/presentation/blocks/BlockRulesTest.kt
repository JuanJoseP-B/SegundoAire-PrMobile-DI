package com.segundoaire.presentation.blocks

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

class BlockRulesTest {

    private val center = Offset(195f, 330f)
    private val radius = 120f

    @Test
    fun `el reloj del bloque muestra minutos sin cero y segundos con dos cifras`() {
        assertEquals("12:04", BlockRules.clockText(724))
        assertEquals("0:09", BlockRules.clockText(9))
        assertEquals("30:00", BlockRules.clockText(1800))
        assertEquals("0:00", BlockRules.clockText(-5))
    }

    @Test
    fun `la punta empieza arriba y gira en el sentido del reloj`() {
        assertOffset(Offset(195f, 210f), BlockRules.tipPosition(0f, center, radius))
        assertOffset(Offset(315f, 330f), BlockRules.tipPosition(0.25f, center, radius))
        assertOffset(Offset(195f, 450f), BlockRules.tipPosition(0.5f, center, radius))
        assertOffset(Offset(75f, 330f), BlockRules.tipPosition(0.75f, center, radius))
        assertOffset(Offset(195f, 210f), BlockRules.tipPosition(1f, center, radius))
    }

    @Test
    fun `un avance fuera de rango se acota`() {
        assertOffset(BlockRules.tipPosition(1f, center, radius), BlockRules.tipPosition(1.4f, center, radius))
        assertOffset(BlockRules.tipPosition(0f, center, radius), BlockRules.tipPosition(-0.2f, center, radius))
    }

    private fun assertOffset(expected: Offset, actual: Offset) {
        assertEquals(expected.x, actual.x, 0.01f)
        assertEquals(expected.y, actual.y, 0.01f)
    }
}
