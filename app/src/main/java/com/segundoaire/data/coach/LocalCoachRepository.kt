package com.segundoaire.data.coach

import com.segundoaire.domain.model.BlockSession
import com.segundoaire.domain.model.CoachMessage
import com.segundoaire.domain.model.CoachTopic
import com.segundoaire.domain.model.InterceptionEvent
import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.domain.repository.BlockSessionRepository
import com.segundoaire.domain.repository.CoachRepository
import com.segundoaire.domain.repository.InterceptionEventRepository
import com.segundoaire.domain.repository.UserProfileRepository
import com.segundoaire.domain.usecase.HoraMasDificilUseCase
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Coach local: lee la última semana (intercepciones y bloques; los de ejemplo con el
 * modo demo) y arma mensajes con datos reales. La rotación recorre los que tienen
 * datos; sin ninguno, invita al primer bloque.
 */
class LocalCoachRepository @Inject constructor(
    private val interceptions: InterceptionEventRepository,
    private val sessions: BlockSessionRepository,
    private val profiles: UserProfileRepository,
    private val clock: Clock
) : CoachRepository {

    override suspend fun message(topic: CoachTopic?, previous: CoachMessage?): CoachMessage {
        val today = LocalDate.now(clock)
        val from = today.minusDays(WEEK_DAYS - 1L).atStartOfDay(clock.zone).toInstant().toEpochMilli()
        val insights = CoachInsights(
            events = interceptions.observeSince(from).first(),
            blocks = sessions.observeCompletedSince(from).first(),
            today = today,
            zone = clock.zone
        )
        val idea = CoachMessage.GoalIdea(profiles.profile.first().mainGoal)

        return when (topic) {
            CoachTopic.WEEK -> insights.week() ?: CoachMessage.FirstSteps
            CoachTopic.IDEA -> idea
            CoachTopic.HARDEST_TIME -> insights.hardestHour() ?: CoachMessage.FirstSteps
            null -> CoachRotation.next(insights.rotation(idea), previous)
        }
    }

    private companion object {
        const val WEEK_DAYS = 7
    }
}

/** Cálculos puros sobre la semana, aparte para poder probarlos. */
internal class CoachInsights(
    private val events: List<InterceptionEvent>,
    private val blocks: List<BlockSession>,
    private val today: LocalDate,
    private val zone: ZoneId
) {
    private fun Long.date(): LocalDate = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

    /** La hora con más intercepciones; en empate, la más tardía (suele ser la de la noche). */
    fun hardestHour(): CoachMessage.HardestHour? = HoraMasDificilUseCase.hardestHour(events, zone)
        ?.let { (hour, openings) -> CoachMessage.HardestHour(hour = hour, openings = openings) }

    fun pausesToday(): CoachMessage.PausesToday? {
        val respected = events.filter { it.result == InterceptionResult.VOLVIO }
        val todayCount = respected.count { it.timestampMillis.date() == today }
        val yesterdayCount = respected.count { it.timestampMillis.date() == today.minusDays(1) }
        return if (todayCount == 0 && yesterdayCount == 0) null else CoachMessage.PausesToday(todayCount, yesterdayCount)
    }

    fun week(): CoachMessage.WeekSummary? {
        val minutes = blocks.sumOf { it.durationMinutes }
        val pauses = events.count { it.result == InterceptionResult.VOLVIO }
        return if (minutes == 0 && pauses == 0) null else CoachMessage.WeekSummary(minutes, pauses)
    }

    /** La intención más repetida de la semana, si se eligió al menos [MIN_INTENT_TIMES] veces. */
    fun intentPattern(): CoachMessage.IntentPattern? = events
        .mapNotNull { it.intent }
        .groupingBy { it }
        .eachCount()
        .maxByOrNull { it.value }
        ?.takeIf { it.value >= MIN_INTENT_TIMES }
        ?.let { CoachMessage.IntentPattern(it.key, it.value) }

    /** Mensajes con datos y la idea del día; sin datos, primero se invita a empezar. */
    fun rotation(idea: CoachMessage.GoalIdea): List<CoachMessage> {
        val withData = listOfNotNull(hardestHour(), intentPattern(), pausesToday())
        return if (withData.isEmpty()) listOf(CoachMessage.FirstSteps, idea) else withData + idea
    }
}

private const val MIN_INTENT_TIMES = 2

/** El siguiente mensaje de la rotación: el que sigue al tipo de `previous`, dando la vuelta. */
internal object CoachRotation {
    fun next(candidates: List<CoachMessage>, previous: CoachMessage?): CoachMessage {
        require(candidates.isNotEmpty()) { "La rotación necesita al menos un mensaje" }
        val index = previous?.let { prev -> candidates.indexOfFirst { it::class == prev::class } } ?: -1
        return candidates[(index + 1) % candidates.size]
    }
}
