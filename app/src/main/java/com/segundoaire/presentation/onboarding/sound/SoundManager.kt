package com.segundoaire.presentation.onboarding.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sonidos de ambiente del onboarding, sintetizados con [SoundSynth] y reproducidos con
 * `AudioTrack` como sonificación: respetan el volumen del sistema y el modo silencio del
 * teléfono. Son bajos a propósito; no son notificaciones.
 */
@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val cache = ConcurrentHashMap<Sfx, ShortArray>()

    /** Interruptor de la UI; encendido por defecto. */
    @Volatile
    var enabled: Boolean = true

    fun play(sfx: Sfx) {
        if (!enabled || !isRingerNormal()) return
        scope.launch {
            val pcm = cache.getOrPut(sfx) { SoundSynth.render(sfx) }
            val track = try {
                buildTrack(pcm)
            } catch (_: IllegalArgumentException) {
                return@launch
            } catch (_: UnsupportedOperationException) {
                return@launch
            }
            try {
                track.write(pcm, 0, pcm.size)
                track.play()
                delay(pcm.size * 1000L / SoundSynth.SampleRate + ReleaseGraceMillis)
            } finally {
                track.release()
            }
        }
    }

    // En silencio o vibración no suena nada, aunque el interruptor de la UI esté encendido
    private fun isRingerNormal(): Boolean {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return true
        return audio.ringerMode == AudioManager.RINGER_MODE_NORMAL
    }

    private fun buildTrack(pcm: ShortArray): AudioTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SoundSynth.SampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setBufferSizeInBytes(pcm.size * Short.SIZE_BYTES)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

    private companion object {
        const val ReleaseGraceMillis = 150L
    }
}
