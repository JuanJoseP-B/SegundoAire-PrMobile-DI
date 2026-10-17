package com.segundoaire.presentation.onboarding

/**
 * Los 14 pasos del flujo "Lo que crece cuando pausas", en el orden del prototipo. Las tres
 * primeras son la bienvenida (semilla, brote y copa); los pasos 3 a 11 llevan "Paso N de 9".
 */
internal object OnboardingStep {
    const val Seed = 0
    const val Sprout = 1
    const val Canopy = 2
    const val Name = 3
    const val Goals = 4
    const val Usage = 5
    const val Moment = 6
    const val Reveal = 7
    const val Breath = 8
    const val Plan = 9
    const val Commit = 10
    const val Permissions = 11
    const val Login = 12
    const val Done = 13

    const val Last = Done
    const val WelcomeLast = Canopy

    /** Pasos numerados "Paso N de 9": del nombre a los permisos. */
    const val QuestionCount = 9

    /** Número del paso en "Paso N de 9" (1 en el nombre, 9 en los permisos). */
    fun number(step: Int): Int = (step - 2).coerceIn(1, QuestionCount)
}
