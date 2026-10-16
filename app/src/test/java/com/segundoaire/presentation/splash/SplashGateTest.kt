package com.segundoaire.presentation.splash

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SplashGateTest {

    @Test
    fun `retiene el splash mientras falta el tema o el destino`() {
        assertTrue(SplashGate.keepOnScreen(themeLoaded = false, destinationLoaded = false, elapsedMillis = 0))
        assertTrue(SplashGate.keepOnScreen(themeLoaded = true, destinationLoaded = false, elapsedMillis = 200))
        assertTrue(SplashGate.keepOnScreen(themeLoaded = false, destinationLoaded = true, elapsedMillis = 200))
    }

    @Test
    fun `suelta el splash cuando ambos estan cargados`() {
        assertFalse(SplashGate.keepOnScreen(themeLoaded = true, destinationLoaded = true, elapsedMillis = 50))
    }

    @Test
    fun `suelta el splash a 1 s aunque falte algo`() {
        assertTrue(SplashGate.keepOnScreen(themeLoaded = false, destinationLoaded = false, elapsedMillis = 999))
        assertFalse(SplashGate.keepOnScreen(themeLoaded = false, destinationLoaded = false, elapsedMillis = 1000))
    }
}
