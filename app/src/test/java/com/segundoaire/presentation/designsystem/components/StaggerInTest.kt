package com.segundoaire.presentation.designsystem.components

import org.junit.Assert.assertEquals
import org.junit.Test

/** Retraso de la entrada escalonada: 40 ms por posición y ninguno con reducir movimiento. */
class StaggerInTest {

    @Test
    fun `el primero aparece sin espera`() =
        assertEquals(0, staggerDelayMillis(index = 0, reduced = false))

    @Test
    fun `cada posicion suma 40 ms`() {
        assertEquals(40, staggerDelayMillis(index = 1, reduced = false))
        assertEquals(280, staggerDelayMillis(index = 7, reduced = false))
    }

    @Test
    fun `con reducir movimiento aparecen todos a la vez`() =
        assertEquals(0, staggerDelayMillis(index = 7, reduced = true))

    @Test
    fun `un indice negativo no adelanta la entrada`() =
        assertEquals(0, staggerDelayMillis(index = -3, reduced = false))

    @Test
    fun `el paso se puede cambiar - la bienvenida usa 120 ms`() {
        assertEquals(120, staggerDelayMillis(index = 1, reduced = false, stepMillis = 120))
        assertEquals(240, staggerDelayMillis(index = 2, reduced = false, stepMillis = 120))
    }

    @Test
    fun `con reducir movimiento el paso tampoco espera`() =
        assertEquals(0, staggerDelayMillis(index = 2, reduced = true, stepMillis = 120))
}
