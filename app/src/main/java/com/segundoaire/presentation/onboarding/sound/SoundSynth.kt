package com.segundoaire.presentation.onboarding.sound

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

/** Los sonidos de la app. El teclado no tiene: solo una háptica leve. */
enum class Sfx {
    /** Avanzar de paso: viento suave y dos notas. */
    Step,

    /** Elegir momento o activar un permiso. */
    Pop,

    /** Gota de agua: quitar una meta, elegir horas y marcas del compromiso. Reemplaza al "tick". */
    Drop,

    /** Elegir una meta: arpegio de tres notas. */
    Bloom,

    /** Completar el compromiso y cerrar el login: acorde abierto. */
    Commit,

    /** Una tercera meta no permitida: tono grave muy suave. */
    Deny,

    /** Entrar a la respiración del GlassWall: dos notas largas (Sol4 y Re5). */
    Calm
}

/** Un tono: [endFrequency] hace un barrido exponencial; [triangle] cambia la onda. */
internal class Tone(
    val frequency: Float,
    val durationSec: Float,
    val gain: Float,
    val delaySec: Float = 0f,
    val endFrequency: Float? = null,
    val triangle: Boolean = false
) {
    val endSec: Float get() = delaySec + durationSec
}

/**
 * Síntesis de los sonidos del prototipo (Web Audio) a muestras PCM de 16 bits: el mismo
 * ataque de 15 ms, la misma caída exponencial y los mismos barridos. Sin archivos de audio.
 * Es matemática pura, para poder probarla sin un dispositivo.
 */
internal object SoundSynth {
    const val SampleRate = 22_050

    /** Volumen general. Los sonidos son de ambiente: bajos a propósito. */
    const val MasterGain = 2.5f

    const val AttackSec = 0.015f
    const val Floor = 0.0001f
    private const val TailSec = 0.05f

    private const val WindSec = 1.2f
    private const val WindPeakGain = 0.035f
    private const val WindPeakSec = 0.4f
    private const val WindSweepSec = 0.6f
    private const val WindFromHz = 400f
    private const val WindToHz = 900f
    private const val WindQ = 0.8f
    private const val FilterBlock = 32

    fun tones(sfx: Sfx): List<Tone> = when (sfx) {
        Sfx.Step -> listOf(Tone(523f, 0.5f, 0.03f, 0.05f), Tone(784f, 0.6f, 0.025f, 0.14f))
        Sfx.Pop -> listOf(Tone(740f, 0.14f, 0.04f, endFrequency = 1180f))
        // Seno de 620 Hz que baja a 470 Hz en 320 ms, más un armónico muy bajo (1240 → 940 Hz)
        Sfx.Drop -> listOf(
            Tone(620f, 0.32f, 0.03f, endFrequency = 470f),
            Tone(1240f, 0.12f, 0.006f, endFrequency = 940f)
        )
        Sfx.Bloom -> listOf(
            Tone(659f, 0.5f, 0.03f),
            Tone(784f, 0.5f, 0.03f, 0.08f),
            Tone(988f, 0.7f, 0.03f, 0.16f)
        )
        Sfx.Commit -> listOf(
            Tone(392f, 1.4f, 0.035f),
            Tone(494f, 1.4f, 0.03f, 0.1f),
            Tone(587f, 1.6f, 0.03f, 0.2f),
            Tone(784f, 1.8f, 0.025f, 0.35f)
        )
        Sfx.Deny -> listOf(Tone(220f, 0.18f, 0.03f, endFrequency = 180f, triangle = true))
        Sfx.Calm -> listOf(Tone(392f, 1.6f, 0.025f), Tone(587f, 1.8f, 0.02f, 0.15f))
    }

    fun hasWind(sfx: Sfx): Boolean = sfx == Sfx.Step

    /** Duración total del sonido, con una pequeña cola. */
    fun durationSec(sfx: Sfx): Float {
        val tonesEnd = tones(sfx).maxOfOrNull { it.endSec } ?: 0f
        return max(tonesEnd, if (hasWind(sfx)) WindSec else 0f) + TailSec
    }

    /** Ganancia en el instante [t] de un tono: sube lineal 15 ms y cae exponencial hasta casi cero. */
    fun envelope(t: Float, durationSec: Float, gain: Float): Float = when {
        t < 0f || t > durationSec -> 0f
        t <= AttackSec -> gain * (t / AttackSec)
        else -> gain * (Floor / gain).pow((t - AttackSec) / (durationSec - AttackSec))
    }

    /** Frecuencia en el instante [t] de un tono; el barrido es exponencial, como el del prototipo. */
    fun frequencyAt(tone: Tone, t: Float): Float {
        val end = tone.endFrequency ?: return tone.frequency
        return tone.frequency * (end / tone.frequency).pow((t / tone.durationSec).coerceIn(0f, 1f))
    }

    /** Muestras del sonido, con [MasterGain] aplicado y recortadas a rango. */
    fun render(sfx: Sfx, sampleRate: Int = SampleRate): ShortArray {
        val count = ceil(durationSec(sfx) * sampleRate).toInt()
        val mix = FloatArray(count)

        tones(sfx).forEach { tone ->
            val start = (tone.delaySec * sampleRate).toInt()
            val length = ((tone.durationSec + TailSec) * sampleRate).toInt()
            var phase = 0.0
            for (i in 0 until length) {
                val index = start + i
                if (index >= count) break
                val t = i.toFloat() / sampleRate
                phase += 2.0 * PI * frequencyAt(tone, t) / sampleRate
                val wave = if (tone.triangle) (asin(sin(phase)) * 2.0 / PI).toFloat() else sin(phase).toFloat()
                mix[index] += wave * envelope(t, tone.durationSec, tone.gain)
            }
        }
        if (hasWind(sfx)) addWind(mix, sampleRate)

        return ShortArray(count) { i ->
            (mix[i] * MasterGain).coerceIn(-1f, 1f).times(Short.MAX_VALUE).toInt().toShort()
        }
    }

    // Ruido blanco por un filtro pasa-banda (400 → 900 Hz en 0,6 s, Q 0,8) con una gota de volumen
    private fun addWind(mix: FloatArray, sampleRate: Int) {
        val random = java.util.Random(WindSeed)
        var x1 = 0.0
        var x2 = 0.0
        var y1 = 0.0
        var y2 = 0.0
        var b0 = 0.0
        var b2 = 0.0
        var a0 = 1.0
        var a1 = 0.0
        var a2 = 0.0
        val samples = minOf(mix.size, (WindSec * sampleRate).toInt())
        for (i in 0 until samples) {
            val t = i.toFloat() / sampleRate
            if (i % FilterBlock == 0) {
                val hz = WindFromHz + (WindToHz - WindFromHz) * (t / WindSweepSec).coerceIn(0f, 1f)
                val w0 = 2.0 * PI * hz / sampleRate
                val alpha = sin(w0) / (2.0 * WindQ)
                b0 = alpha
                b2 = -alpha
                a0 = 1.0 + alpha
                a1 = -2.0 * cos(w0)
                a2 = 1.0 - alpha
            }
            val x = random.nextDouble() * 2.0 - 1.0
            val y = (b0 * x + b2 * x2 - a1 * y1 - a2 * y2) / a0
            x2 = x1
            x1 = x
            y2 = y1
            y1 = y
            val gain = if (t < WindPeakSec) {
                WindPeakGain * t / WindPeakSec
            } else {
                WindPeakGain * (1f - (t - WindPeakSec) / (WindSec - WindPeakSec)).coerceAtLeast(0f)
            }
            mix[i] += (y * gain).toFloat()
        }
    }

    private const val WindSeed = 20_240_614L
}
