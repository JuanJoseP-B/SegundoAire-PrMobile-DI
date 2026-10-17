package com.segundoaire.presentation.panel

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.segundoaire.R
import com.segundoaire.domain.model.CoachAction
import com.segundoaire.domain.model.CoachMessage
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.InterceptionIntent
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** El texto del mensaje en el idioma del usuario. */
@Composable
internal fun coachText(message: CoachMessage): String = when (message) {
    is CoachMessage.HardestHour -> pluralStringResource(
        R.plurals.coach_msg_hardest,
        message.openings,
        hourText(message.hour),
        message.openings
    )
    is CoachMessage.PausesToday -> when {
        message.today == 0 -> pluralStringResource(R.plurals.coach_msg_pauses_yesterday, message.yesterday, message.yesterday)
        message.today > message.yesterday -> pluralStringResource(R.plurals.coach_msg_pauses_more, message.today, message.today)
        else -> pluralStringResource(R.plurals.coach_msg_pauses_today, message.today, message.today)
    }
    is CoachMessage.WeekSummary -> pluralStringResource(
        R.plurals.coach_msg_week,
        message.pauses,
        durationText(message.minutes),
        message.pauses
    )
    is CoachMessage.IntentPattern -> pluralStringResource(
        when (message.intent) {
            InterceptionIntent.BORED -> R.plurals.coach_msg_intent_bored
            InterceptionIntent.REPLY_MESSAGE -> R.plurals.coach_msg_intent_reply
            InterceptionIntent.SEARCH -> R.plurals.coach_msg_intent_search
        },
        message.times,
        message.times
    )
    is CoachMessage.GoalIdea -> stringResource(ideaRes(message.goal))
    CoachMessage.FirstSteps -> stringResource(R.string.coach_msg_first)
}

/** Etiqueta del botón: "Otra idea" y "Ahora no" son la misma acción con distinto tono. */
@Composable
internal fun coachActionLabel(message: CoachMessage, action: CoachAction): String = stringResource(
    when (action) {
        CoachAction.CREATE_BLOCK -> R.string.coach_action_create_block
        CoachAction.START_15 -> R.string.coach_action_start_15
        CoachAction.VIEW_PROGRESS -> R.string.coach_action_progress
        CoachAction.REMIND -> R.string.coach_action_remind
        CoachAction.NEXT ->
            if (message is CoachMessage.GoalIdea || message is CoachMessage.IntentPattern) {
                R.string.coach_action_other_idea
            } else {
                R.string.coach_action_not_now
            }
    }
)

/** "21:00" en español, "9:00 PM" en inglés: el formato corto del idioma. */
@Composable
internal fun hourText(hour: Int): String =
    LocalTime.of(hour.coerceIn(0, 23), 0).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

/** "45 min" o "3 h 14 min". */
@Composable
internal fun durationText(minutes: Int): String {
    val hours = minutes / 60
    return if (hours == 0) {
        stringResource(R.string.duration_minutes, minutes)
    } else {
        stringResource(R.string.duration_hours_minutes, hours, minutes % 60)
    }
}

@StringRes
private fun ideaRes(goal: Goal?): Int = when (goal) {
    Goal.FREE_TIME -> R.string.coach_msg_idea_free_time
    Goal.SLEEP -> R.string.coach_msg_idea_sleep
    Goal.FOCUS -> R.string.coach_msg_idea_focus
    Goal.PRESENCE -> R.string.coach_msg_idea_presence
    Goal.MENTAL_HEALTH -> R.string.coach_msg_idea_mental_health
    null -> R.string.coach_msg_idea_general
}
