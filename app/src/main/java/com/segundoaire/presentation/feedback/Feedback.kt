package com.segundoaire.presentation.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.segundoaire.presentation.onboarding.sound.Haptics
import com.segundoaire.presentation.onboarding.sound.Sfx
import com.segundoaire.presentation.onboarding.sound.SoundManager
import com.segundoaire.presentation.onboarding.sound.rememberHaptics

/**
 * El [SoundManager] de la app para las pantallas. Lo proveen `MainActivity` y la ventana
 * del GlassWall; en previews y en el catálogo puede faltar y entonces no suena nada.
 */
val LocalSoundManager = staticCompositionLocalOf<SoundManager?> { null }

/**
 * Sonido y háptica de la app principal y del GlassWall (DESIGN_SYSTEM.md §14). Todo se
 * apaga desde Perfil → Sonidos y vibración, y el sonido respeta además el modo silencio.
 *
 * | Evento | Sonido | Háptica |
 * |---|---|---|
 * | Duración, modo, día, toggle; acciones de la IA | gota | `ContextClick` |
 * | Iniciar bloque | acorde | 20 / 30 / 40 ms |
 * | Entrar a la respiración del GlassWall | calma | — |
 * | Volver a mi bloque | gota (y arpegio al brotar la hoja) | `Confirm` |
 * | Brotar una hoja, terminar un bloque completo | arpegio | `Confirm` |
 */
@Stable
class Feedback(private val sound: SoundManager?, private val haptics: Haptics) {
    private val enabled: Boolean get() = sound?.enabled ?: false

    fun select() {
        sound?.play(Sfx.Drop)
        if (enabled) haptics.contextClick()
    }

    fun startBlock() {
        sound?.play(Sfx.Commit)
        if (enabled) haptics.startBlock()
    }

    fun calm() {
        sound?.play(Sfx.Calm)
    }

    fun back() {
        sound?.play(Sfx.Drop)
        if (enabled) haptics.confirm()
    }

    fun bloom() {
        sound?.play(Sfx.Bloom)
        if (enabled) haptics.confirm()
    }
}

@Composable
fun rememberFeedback(): Feedback {
    val sound = LocalSoundManager.current
    val haptics = rememberHaptics()
    return remember(sound, haptics) { Feedback(sound, haptics) }
}
