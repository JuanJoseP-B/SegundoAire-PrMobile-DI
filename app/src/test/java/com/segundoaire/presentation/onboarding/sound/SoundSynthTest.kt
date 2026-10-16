package com.segundoaire.presentation.onboarding.sound

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundSynthTest {

    private val drop = SoundSynth.tones(Sfx.Drop)

    @Test
    fun `la gota es un seno de 620 Hz que baja a 470 Hz en unos 320 ms`() {
        val main = drop[0]

        assertEquals(620f, main.frequency, 0f)
        assertEquals(470f, main.endFrequency!!, 0f)
        assertEquals(0.32f, main.durationSec, 0.0001f)
        assertFalse(main.triangle)
    }

    @Test
    fun `la gota lleva un armonico muy bajo de 1240 a 940 Hz en 120 ms`() {
        val harmonic = drop[1]

        assertEquals(1240f, harmonic.frequency, 0f)
        assertEquals(940f, harmonic.endFrequency!!, 0f)
        assertEquals(0.12f, harmonic.durationSec, 0.0001f)
        assertTrue(harmonic.gain < drop[0].gain / 2f)
    }

    @Test
    fun `ningun sonido es el pitido agudo de 1300 Hz del tick`() {
        Sfx.entries.forEach { sfx ->
            SoundSynth.tones(sfx).forEach { tone ->
                assertNotEquals("$sfx", 1300f, tone.frequency, 0f)
                assertFalse("$sfx", tone.triangle && tone.frequency > 1000f)
            }
        }
    }

    @Test
    fun `la gota dura menos de 400 ms de cola incluida`() {
        assertTrue(SoundSynth.durationSec(Sfx.Drop) < 0.4f)
    }

    @Test
    fun `el barrido de frecuencia es exponencial entre los dos extremos`() {
        val main = drop[0]

        assertEquals(620f, SoundSynth.frequencyAt(main, 0f), 0.01f)
        assertEquals(470f, SoundSynth.frequencyAt(main, main.durationSec), 0.01f)
        // A mitad, la media geométrica
        assertEquals(Math.sqrt(620.0 * 470.0).toFloat(), SoundSynth.frequencyAt(main, main.durationSec / 2f), 0.01f)
    }

    @Test
    fun `la envolvente sube en 15 ms y cae hasta casi cero`() {
        val gain = 0.03f

        assertEquals(0f, SoundSynth.envelope(0f, 0.3f, gain), 0f)
        assertEquals(gain, SoundSynth.envelope(SoundSynth.AttackSec, 0.3f, gain), 0.0001f)
        assertEquals(SoundSynth.Floor, SoundSynth.envelope(0.3f, 0.3f, gain), 0.00001f)
        assertEquals(0f, SoundSynth.envelope(0.31f, 0.3f, gain), 0f)
    }

    @Test
    fun `solo avanzar de paso lleva viento`() {
        assertTrue(SoundSynth.hasWind(Sfx.Step))
        assertTrue(Sfx.entries.filter { it != Sfx.Step }.none(SoundSynth::hasWind))
    }

    @Test
    fun `avanzar dura lo que el viento y el compromiso lo que su acorde`() {
        assertEquals(1.25f, SoundSynth.durationSec(Sfx.Step), 0.001f)
        assertEquals(0.35f + 1.8f + 0.05f, SoundSynth.durationSec(Sfx.Commit), 0.001f)
    }

    @Test
    fun `el tono de rechazo es grave y de onda triangular`() {
        val deny = SoundSynth.tones(Sfx.Deny).single()

        assertEquals(220f, deny.frequency, 0f)
        assertEquals(180f, deny.endFrequency!!, 0f)
        assertTrue(deny.triangle)
    }

    @Test
    fun `cada sonido se sintetiza con muestras audibles y dentro de rango`() {
        Sfx.entries.forEach { sfx ->
            val pcm = SoundSynth.render(sfx)

            assertEquals("$sfx", kotlin.math.ceil(SoundSynth.durationSec(sfx) * SoundSynth.SampleRate).toInt(), pcm.size)
            assertTrue("$sfx suena", pcm.any { it.toInt() != 0 })
            // Son sonidos de ambiente: ninguno pasa de la mitad de la escala
            assertTrue("$sfx es bajo", pcm.maxOf { kotlin.math.abs(it.toInt()) } < Short.MAX_VALUE / 2)
        }
    }
}
