package com.segundoaire.presentation.blocks

import androidx.compose.ui.geometry.Offset
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Reglas del bloque sin dibujo, aparte para poder probarlas. */
internal object BlockRules {
    /** "m:ss" del tiempo restante: los minutos sin cero a la izquierda, como en la referencia. */
    fun clockText(totalSeconds: Int): String {
        val seconds = totalSeconds.coerceAtLeast(0)
        return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60)
    }

    /**
     * Punta del anillo para el avance [progress] (0–1): arranca arriba (−90°) y gira en
     * el sentido del reloj sobre la circunferencia de [radius] alrededor de [center].
     */
    fun tipPosition(progress: Float, center: Offset, radius: Float): Offset {
        val angle = -PI / 2 + progress.coerceIn(0f, 1f) * 2 * PI
        return Offset(center.x + (cos(angle) * radius).toFloat(), center.y + (sin(angle) * radius).toFloat())
    }
}
