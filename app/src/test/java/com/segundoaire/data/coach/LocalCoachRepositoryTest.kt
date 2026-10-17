package com.segundoaire.data.coach

import com.segundoaire.domain.model.BlockSession
import com.segundoaire.domain.model.CoachMessage
import com.segundoaire.domain.model.CoachTopic
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.InterceptionEvent
import com.segundoaire.domain.model.InterceptionIntent
import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.testing.FakeBlockSessionRepository
import com.segundoaire.testing.FakeInterceptionEventRepository
import com.segundoaire.testing.FakeUserProfileRepository
import com.segundoaire.testing.MutableClock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class LocalCoachRepositoryTest {

    private val clock = MutableClock(Instant.parse("2026-10-16T15:00:00Z"))
    private val interceptions = FakeInterceptionEventRepository()
    private val sessions = FakeBlockSessionRepository()
    private val profiles = FakeUserProfileRepository()
    private val repository = LocalCoachRepository(interceptions, sessions, profiles, clock)

    private fun at(time: String) = Instant.parse(time).toEpochMilli()

    private fun event(id: Long, time: String, result: InterceptionResult? = InterceptionResult.VOLVIO) =
        InterceptionEvent(id, "com.example.video", at(time), result)

    @Test
    fun `la hora mas dificil es la de mas intercepciones y en empate la mas tardia`() {
        val insights = CoachInsights(
            events = listOf(
                event(1, "2026-10-14T21:10:00Z"), event(2, "2026-10-15T21:40:00Z"),
                event(3, "2026-10-15T08:00:00Z"), event(4, "2026-10-16T08:30:00Z")
            ),
            blocks = emptyList(),
            today = clock.instant.atZone(clock.zone).toLocalDate(),
            zone = clock.zone
        )

        assertEquals(CoachMessage.HardestHour(hour = 21, openings = 2), insights.hardestHour())
    }

    @Test
    fun `las pausas de hoy se comparan con las de ayer`() {
        val insights = CoachInsights(
            events = listOf(
                event(1, "2026-10-16T09:00:00Z"), event(2, "2026-10-16T10:00:00Z"),
                event(3, "2026-10-15T09:00:00Z"),
                event(4, "2026-10-16T11:00:00Z", InterceptionResult.CONTINUO)
            ),
            blocks = emptyList(),
            today = clock.instant.atZone(clock.zone).toLocalDate(),
            zone = clock.zone
        )

        assertEquals(CoachMessage.PausesToday(today = 2, yesterday = 1), insights.pausesToday())
    }

    @Test
    fun `sin pausas hoy ni ayer no hay mensaje de pausas`() {
        val insights = CoachInsights(emptyList(), emptyList(), clock.instant.atZone(clock.zone).toLocalDate(), clock.zone)

        assertNull(insights.pausesToday())
        assertNull(insights.week())
        assertNull(insights.hardestHour())
    }

    @Test
    fun `la intencion mas repetida aparece desde dos veces`() {
        val today = clock.instant.atZone(clock.zone).toLocalDate()
        fun withIntent(id: Long, intent: InterceptionIntent) = event(id, "2026-10-16T09:00:00Z").copy(intent = intent)
        val once = CoachInsights(listOf(withIntent(1, InterceptionIntent.BORED)), emptyList(), today, clock.zone)
        val twice = CoachInsights(
            listOf(
                withIntent(1, InterceptionIntent.BORED),
                withIntent(2, InterceptionIntent.SEARCH),
                withIntent(3, InterceptionIntent.BORED)
            ),
            emptyList(),
            today,
            clock.zone
        )

        assertNull(once.intentPattern())
        assertEquals(CoachMessage.IntentPattern(InterceptionIntent.BORED, 2), twice.intentPattern())
    }

    @Test
    fun `la rotacion sigue al tipo anterior y da la vuelta`() {
        val list = listOf(CoachMessage.HardestHour(21, 2), CoachMessage.PausesToday(1, 0), CoachMessage.GoalIdea(null))

        assertEquals(list[0], CoachRotation.next(list, previous = null))
        assertEquals(list[1], CoachRotation.next(list, previous = CoachMessage.HardestHour(8, 9)))
        assertEquals(list[0], CoachRotation.next(list, previous = CoachMessage.GoalIdea(Goal.SLEEP)))
        assertEquals(list[0], CoachRotation.next(list, previous = CoachMessage.FirstSteps))
    }

    @Test
    fun `sin datos primero invita a empezar y luego da una idea`() = runTest {
        profiles.saveProfile(UserProfile(goals = listOf(Goal.SLEEP)))

        val first = repository.message(topic = null, previous = null)
        val second = repository.message(topic = null, previous = first)

        assertEquals(CoachMessage.FirstSteps, first)
        assertEquals(CoachMessage.GoalIdea(Goal.SLEEP), second)
    }

    @Test
    fun `cada tema responde con su mensaje`() = runTest {
        interceptions.events.value = listOf(event(1, "2026-10-16T21:00:00Z"))
        sessions.sessions.value = listOf(
            BlockSession(at("2026-10-15T10:00:00Z"), at("2026-10-15T10:30:00Z"), 30, RigorLevel.MINDFUL, completed = true)
        )
        profiles.saveProfile(UserProfile(goals = listOf(Goal.FOCUS)))

        assertEquals(CoachMessage.WeekSummary(minutes = 30, pauses = 1), repository.message(CoachTopic.WEEK, null))
        assertEquals(CoachMessage.GoalIdea(Goal.FOCUS), repository.message(CoachTopic.IDEA, null))
        assertEquals(CoachMessage.HardestHour(21, 1), repository.message(CoachTopic.HARDEST_TIME, null))
    }

    @Test
    fun `sin datos de la semana el tema de la semana invita a empezar`() = runTest {
        assertEquals(CoachMessage.FirstSteps, repository.message(CoachTopic.WEEK, null))
        assertEquals(CoachMessage.FirstSteps, repository.message(CoachTopic.HARDEST_TIME, null))
    }

    @Test
    fun `solo mira los ultimos siete dias`() = runTest {
        interceptions.events.value = listOf(event(1, "2026-10-09T21:00:00Z"))

        assertEquals(CoachMessage.FirstSteps, repository.message(CoachTopic.HARDEST_TIME, null))
    }
}
