package com.segundoaire.presentation.onboarding

import androidx.annotation.StringRes
import com.segundoaire.R
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.RiskMoment
import com.segundoaire.domain.model.UsageRange
import com.segundoaire.presentation.designsystem.components.Rigor

// El dominio no conoce los recursos: aquí se asocia cada valor del perfil a su cadena.

@get:StringRes
internal val Goal.labelRes: Int
    get() = when (this) {
        Goal.FOCUS -> R.string.onboarding_goal_focus
        Goal.SLEEP -> R.string.onboarding_goal_sleep
        Goal.PRESENCE -> R.string.onboarding_goal_presence
        Goal.MENTAL_HEALTH -> R.string.onboarding_goal_mental_health
        Goal.FREE_TIME -> R.string.onboarding_goal_free_time
    }

@get:StringRes
internal val UsageRange.labelRes: Int
    get() = when (this) {
        UsageRange.UNDER_2 -> R.string.onboarding_usage_under_2
        UsageRange.FROM_2_TO_4 -> R.string.onboarding_usage_2_to_4
        UsageRange.FROM_4_TO_6 -> R.string.onboarding_usage_4_to_6
        UsageRange.FROM_6_TO_8 -> R.string.onboarding_usage_6_to_8
        UsageRange.OVER_8 -> R.string.onboarding_usage_over_8
    }

@get:StringRes
internal val RiskMoment.labelRes: Int
    get() = when (this) {
        RiskMoment.WAKING_UP -> R.string.onboarding_moment_waking_up
        RiskMoment.BEFORE_SLEEP -> R.string.onboarding_moment_before_sleep
        RiskMoment.STUDY_OR_WORK -> R.string.onboarding_moment_study_or_work
        RiskMoment.BORED -> R.string.onboarding_moment_bored
    }

/** Rigor del dominio como el nivel que entienden los componentes del design system. */
internal fun RigorLevel.toRigor(): Rigor = when (this) {
    RigorLevel.MINDFUL -> Rigor.Mindful
    RigorLevel.STRICT -> Rigor.Strict
}
