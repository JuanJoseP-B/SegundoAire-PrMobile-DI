package com.segundoaire.presentation.progress

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressRulesTest {

    @Test
    fun `un dia florece desde los 30 min`() {
        assertFalse(ProgressRules.hasFlower(0))
        assertFalse(ProgressRules.hasFlower(29))
        assertTrue(ProgressRules.hasFlower(30))
        assertTrue(ProgressRules.hasFlower(90))
    }

    @Test
    fun `el grosor del anillo es un doceavo de los minutos y nunca menos de 1,5`() {
        assertEquals(1.5f, ProgressRules.ringWidth(0), 0.001f)
        assertEquals(1.5f, ProgressRules.ringWidth(12), 0.001f)
        assertEquals(2.5f, ProgressRules.ringWidth(30), 0.001f)
        assertEquals(5f, ProgressRules.ringWidth(60), 0.001f)
    }

    @Test
    fun `los anillos crecen de 9 en 9 desde el centro y se mecen`() {
        assertEquals(23f, ProgressRules.ringRadiusX(0), 0.001f)
        assertEquals(122f, ProgressRules.ringRadiusX(11), 0.001f)
        assertEquals(listOf(-1, 0, 1, -1), (0..3).map(ProgressRules::ringWobble))
    }

    @Test
    fun `el tallo mide 96 por hora mas 6 y se topa en una hora`() {
        assertEquals(6, ProgressRules.stemHeight(0))
        assertEquals(54, ProgressRules.stemHeight(30))
        assertEquals(102, ProgressRules.stemHeight(60))
        assertEquals(102, ProgressRules.stemHeight(120))
    }

    @Test
    fun `cuantos anillos dibujar`() {
        assertEquals(2, ProgressRules.ringCount(daysSincePlanted = 0, daysThisWeek = 2))
        assertEquals(6, ProgressRules.ringCount(daysSincePlanted = 5, daysThisWeek = 2))
        assertEquals(12, ProgressRules.ringCount(daysSincePlanted = 40, daysThisWeek = 2))
        assertEquals(12, ProgressRules.ringCount(daysSincePlanted = null, daysThisWeek = 2))
    }
}
