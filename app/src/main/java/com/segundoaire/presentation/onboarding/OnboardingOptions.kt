package com.segundoaire.presentation.onboarding

import androidx.annotation.StringRes
import com.segundoaire.R
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.RiskMoment
import com.segundoaire.domain.model.UsageRange

/**
 * Las opciones del prototipo, en su orden, ligadas al valor del perfil que guardan. La
 * posición de cada meta es también la de su flor en el árbol.
 */
internal class GoalOption(
    val goal: Goal,
    @StringRes val label: Int,
    val icon: String,
    val filledIcon: Boolean = true
)

internal val GoalOptions = listOf(
    GoalOption(Goal.FREE_TIME, R.string.onb_goal_social, OnboardingIcons.Leaf),
    GoalOption(Goal.SLEEP, R.string.onb_goal_sleep, OnboardingIcons.Moon),
    GoalOption(Goal.FOCUS, R.string.onb_goal_focus, OnboardingIcons.Flame),
    GoalOption(Goal.PRESENCE, R.string.onb_goal_presence, OnboardingIcons.Branch, filledIcon = false),
    GoalOption(Goal.MENTAL_HEALTH, R.string.onb_goal_anxiety, OnboardingIcons.Drop)
)

internal class MomentOption(
    val moment: RiskMoment,
    @StringRes val label: Int,
    @StringRes val hours: Int,
    @StringRes val block: Int,
    val icon: String
)

internal val MomentOptions = listOf(
    MomentOption(
        RiskMoment.WAKING_UP, R.string.onb_moment_morning, R.string.onb_moment_morning_hours,
        R.string.onb_plan_block_morning, OnboardingIcons.Dawn
    ),
    MomentOption(
        RiskMoment.STUDY_OR_WORK, R.string.onb_moment_afternoon, R.string.onb_moment_afternoon_hours,
        R.string.onb_plan_block_afternoon, OnboardingIcons.Sun
    ),
    MomentOption(
        RiskMoment.BEFORE_SLEEP, R.string.onb_moment_evening, R.string.onb_moment_evening_hours,
        R.string.onb_plan_block_evening, OnboardingIcons.Moon
    ),
    MomentOption(
        RiskMoment.BORED, R.string.onb_moment_late, R.string.onb_moment_late_hours,
        R.string.onb_plan_block_late, OnboardingIcons.Stars
    )
)

internal val UsageLabels = intArrayOf(
    R.string.onb_usage_0, R.string.onb_usage_1, R.string.onb_usage_2, R.string.onb_usage_3, R.string.onb_usage_4
)

internal val UsageCaptions = intArrayOf(
    R.string.onb_usage_cap_0, R.string.onb_usage_cap_1, R.string.onb_usage_cap_2,
    R.string.onb_usage_cap_3, R.string.onb_usage_cap_4
)

/** Posición del rango en el selector (0 = menos de 2 h … 4 = más de 8 h). */
internal val UsageRange.index: Int get() = ordinal

internal fun usageRangeAt(index: Int): UsageRange = UsageRange.entries[index.coerceIn(0, UsageRange.entries.lastIndex)]

/** Metas del perfil como posiciones de las opciones, en el orden en que se eligieron. */
internal fun List<Goal>.toPickIndexes(): List<Int> =
    mapNotNull { goal -> GoalOptions.indexOfFirst { it.goal == goal }.takeIf { it >= 0 } }

internal fun momentIndexOf(moments: Set<RiskMoment>): Int =
    moments.firstOrNull()?.let { moment -> MomentOptions.indexOfFirst { it.moment == moment } } ?: -1
