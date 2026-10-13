package com.segundoaire.domain.model

/** Parte del día que decide el saludo del Panel. */
enum class GreetingPeriod {
    MORNING,
    AFTERNOON,
    EVENING;

    companion object {
        /** De 5 a 11 h es mañana, de 12 a 19 h tarde y el resto noche. */
        fun at(hourOfDay: Int): GreetingPeriod = when (hourOfDay) {
            in 5..11 -> MORNING
            in 12..19 -> AFTERNOON
            else -> EVENING
        }
    }
}
