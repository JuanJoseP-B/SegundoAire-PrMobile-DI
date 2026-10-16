package com.segundoaire.presentation.splash

import com.segundoaire.domain.model.StartDestination
import com.segundoaire.presentation.designsystem.theme.Motion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

class SplashTimelineTest {

    @Test
    fun `la version completa dura 7150 ms`() {
        // 4 tramos de 1000 + bocanada 1000 + tension 150 + liberacion 500 + texto 300 + pausa 1000 + salida 200
        assertEquals(7150L, SplashTimeline.totalMillis(SplashVariant.Full))
    }

    @Test
    fun `la version abreviada dura 1000 ms`() {
        assertEquals(1000L, SplashTimeline.totalMillis(SplashVariant.Short))
    }

    @Test
    fun `con reducir movimiento dura 1200 ms`() {
        assertEquals(1200L, SplashTimeline.totalMillis(SplashVariant.Reduced))
    }

    @Test
    fun `el spring de respiracion se asienta dentro de su tramo`() {
        val spring = SplashTimeline.breathSpringMillis()

        assertTrue("el spring dura $spring ms", spring in 1..Motion.SplashBreathLegMillis.toLong())
        assertEquals(Motion.SplashBreathLegMillis.toLong(), spring + SplashTimeline.breathPauseMillis())
    }

    @Test
    fun `la primera ejecucion y el reinicio muestran la version completa`() {
        assertEquals(
            SplashVariant.Full,
            SplashTimeline.variantFor(StartDestination.ONBOARDING, reducedMotion = false)
        )
    }

    @Test
    fun `con el onboarding completado se muestra la version abreviada`() {
        listOf(StartDestination.ACCOUNT, StartDestination.SETUP, StartDestination.MAIN).forEach {
            assertEquals(SplashVariant.Short, SplashTimeline.variantFor(it, reducedMotion = false))
        }
    }

    @Test
    fun `reducir movimiento manda sobre el destino`() {
        StartDestination.entries.forEach {
            assertEquals(SplashVariant.Reduced, SplashTimeline.variantFor(it, reducedMotion = true))
        }
    }

    @Test
    fun `la liberacion termina cubriendo la diagonal con margen`() {
        val width = 1080f
        val height = 2400f

        val target = releaseTargetScale(width, height)
        val coveredRadius = BlobGeometry.baseRadius(width, height) * target

        assertEquals(hypot(width, height) * 1.15f, coveredRadius, 0.5f)
        // El peor radio del borde (94 %) sigue cubriendo la esquina, que esta a media diagonal del centro
        assertTrue(coveredRadius * (1f - BlobGeometry.AmplitudeFraction) > hypot(width, height) / 2f)
    }

    @Test
    fun `sin tamano la liberacion no divide entre cero`() {
        assertEquals(1f, releaseTargetScale(0f, 0f), 0f)
    }
}
