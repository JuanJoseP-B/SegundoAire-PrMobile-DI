package com.segundoaire.domain.model

/** Lo que el usuario quiere lograr al usar menos el celular. */
enum class Goal { FOCUS, SLEEP, PRESENCE, MENTAL_HEALTH, FREE_TIME }

/**
 * Uso diario estimado. [typicalHoursPerDay] es el punto medio del tramo; en
 * "más de 8 h" se usa el mínimo del tramo para no inflar las proyecciones.
 */
enum class UsageRange(val typicalHoursPerDay: Int) {
    UNDER_2(1),
    FROM_2_TO_4(3),
    FROM_4_TO_6(5),
    FROM_6_TO_8(7),
    OVER_8(8)
}

/** Momento del día en que el usuario más usa el celular. */
enum class RiskMoment { WAKING_UP, BEFORE_SLEEP, STUDY_OR_WORK, BORED }

/** Rigor del GlassWall durante un bloque. */
enum class RigorLevel { MINDFUL, STRICT }

/** Cómo entró el usuario a la cuenta simulada. [GUEST] es "continuar sin cuenta". */
enum class AuthMode { GOOGLE, APPLE, EMAIL, GUEST }

/**
 * Perfil que arma el onboarding. [goals] conserva el orden en que se eligieron:
 * el primero es el objetivo principal que reaparece en el Panel y el GlassWall.
 *
 * [authMode] es `null` hasta que el usuario pasa por la pantalla de cuenta y
 * [email] solo tiene valor con [AuthMode.EMAIL]. La contraseña nunca se guarda.
 *
 * [setupCompleted] pasa a `true` al confirmar las apps del setup; no lo escribe
 * [com.segundoaire.domain.repository.UserProfileRepository.saveProfile].
 */
data class UserProfile(
    val name: String = "",
    val goals: List<Goal> = emptyList(),
    val usageRange: UsageRange? = null,
    val riskMoments: Set<RiskMoment> = emptySet(),
    val suggestedRigor: RigorLevel = RigorLevel.MINDFUL,
    val authMode: AuthMode? = null,
    val email: String = "",
    val setupCompleted: Boolean = false
) {
    val mainGoal: Goal? get() = goals.firstOrNull()

    companion object {
        const val MAX_NAME_LENGTH = 30
        const val MAX_GOALS = 2
    }
}
