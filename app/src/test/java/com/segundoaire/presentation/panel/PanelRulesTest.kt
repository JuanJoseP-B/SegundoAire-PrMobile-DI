package com.segundoaire.presentation.panel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PanelRulesTest {

    @Test
    fun `la luz del heroe sigue las franjas de la referencia`() {
        val expected = mapOf(
            0 to HeroLight.NIGHT, 5 to HeroLight.NIGHT,
            6 to HeroLight.MORNING, 10 to HeroLight.MORNING,
            11 to HeroLight.DAY, 16 to HeroLight.DAY,
            17 to HeroLight.EVENING, 19 to HeroLight.EVENING,
            20 to HeroLight.NIGHT, 23 to HeroLight.NIGHT
        )
        expected.forEach { (hour, light) -> assertEquals("a las $hour h", light, PanelRules.heroLight(hour)) }
    }

    @Test
    fun `las luciernagas salen de 20 a 6`() {
        assertTrue(PanelRules.firefliesVisible(20))
        assertTrue(PanelRules.firefliesVisible(23))
        assertTrue(PanelRules.firefliesVisible(0))
        assertTrue(PanelRules.firefliesVisible(5))
        assertFalse(PanelRules.firefliesVisible(6))
        assertFalse(PanelRules.firefliesVisible(19))
    }
}
