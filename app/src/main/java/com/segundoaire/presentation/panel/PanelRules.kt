package com.segundoaire.presentation.panel

/** Luz radial del héroe según la hora real (`sky` de la referencia). */
enum class HeroLight { MORNING, DAY, EVENING, NIGHT }

/** Reglas del Panel sin dibujo, aparte para poder probarlas. */
internal object PanelRules {
    /** Madrugada y noche (antes de las 6 y desde las 20), mañana hasta las 11, día hasta las 17, tarde hasta las 20. */
    fun heroLight(hour: Int): HeroLight = when {
        hour < 6 -> HeroLight.NIGHT
        hour < 11 -> HeroLight.MORNING
        hour < 17 -> HeroLight.DAY
        hour < 20 -> HeroLight.EVENING
        else -> HeroLight.NIGHT
    }

    /** Las luciérnagas salen entre las 20:00 y las 6:00. */
    fun firefliesVisible(hour: Int): Boolean = hour < 6 || hour >= 20
}
