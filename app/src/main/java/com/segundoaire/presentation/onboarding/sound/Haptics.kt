package com.segundoaire.presentation.onboarding.sound

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

/**
 * Háptica del onboarding. Avanzar es `ContextClick`; florecer y los permisos, `Confirm`
 * (API 30+, un pulso corto antes); las marcas del compromiso, `TextHandleMove`; completarlo,
 * `LongPress` más el patrón 30/40/60 ms; una opción no permitida, `Reject` (API 30+).
 */
@Stable
class Haptics(private val view: View, private val vibrator: Vibrator?) {

    fun contextClick() {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }

    fun confirm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            pulse(ShortPulseMillis)
        }
    }

    fun reject() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            pulse(RejectPulseMillis)
        }
    }

    /** Una vibración muy leve: cada letra del nombre y las marcas del compromiso. */
    fun textHandleMove() {
        view.performHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
    }

    /** Al completar el compromiso: pulsación larga y el patrón 30 / 40 / 60 ms. */
    fun commit() {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        vibrator?.takeIf { it.hasVibrator() }?.vibrate(
            VibrationEffect.createWaveform(CommitPattern, NoRepeat)
        )
    }

    private fun pulse(millis: Long) {
        vibrator?.takeIf { it.hasVibrator() }?.vibrate(
            VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }

    private companion object {
        const val ShortPulseMillis = 20L
        const val RejectPulseMillis = 30L
        const val NoRepeat = -1

        // Espera inicial, vibra 30, pausa 40, vibra 60
        val CommitPattern = longArrayOf(0, 30, 40, 60)
    }
}

@Composable
fun rememberHaptics(): Haptics {
    val view = LocalView.current
    val context = LocalContext.current
    return remember(view) { Haptics(view, context.vibrator()) }
}

private fun Context.vibrator(): Vibrator? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
