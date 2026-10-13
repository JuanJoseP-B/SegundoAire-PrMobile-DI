package com.segundoaire.presentation.designsystem.components

import org.junit.Assert.assertEquals
import org.junit.Test

/** Reloj de RevealText: 18 ms entre caracteres y 150 ms de fundido por carácter. */
class RevealProgressTest {

    private val delay = 18
    private val fade = 150

    @Test
    fun `un caracter no empieza antes de su turno`() =
        assertEquals(0f, revealProgress(index = 5, elapsedMillis = 89f, charDelayMillis = delay, fadeMillis = fade))

    @Test
    fun `a mitad del fundido va por la mitad`() =
        assertEquals(0.5f, revealProgress(index = 5, elapsedMillis = 165f, charDelayMillis = delay, fadeMillis = fade))

    @Test
    fun `termina al pasar su turno mas el fundido`() =
        assertEquals(1f, revealProgress(index = 5, elapsedMillis = 240f, charDelayMillis = delay, fadeMillis = fade))

    @Test
    fun `sin espera entre caracteres todos avanzan a la vez`() =
        assertEquals(
            revealProgress(index = 0, elapsedMillis = 75f, charDelayMillis = 0, fadeMillis = fade),
            revealProgress(index = 40, elapsedMillis = 75f, charDelayMillis = 0, fadeMillis = fade)
        )
}
