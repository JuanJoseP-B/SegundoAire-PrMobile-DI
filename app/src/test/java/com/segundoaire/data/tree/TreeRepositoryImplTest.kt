package com.segundoaire.data.tree

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.InterceptionEvent
import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.testing.FakeInterceptionEventRepository
import com.segundoaire.testing.FakeUserPreferencesRepository
import com.segundoaire.testing.FakeUserProfileRepository
import com.segundoaire.testing.MutableClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.Instant
import java.time.LocalDate

class TreeRepositoryImplTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val scope = TestScope(UnconfinedTestDispatcher())
    private val clock = MutableClock(Instant.parse("2026-10-16T12:00:00Z"))
    private val profiles = FakeUserProfileRepository()
    private val preferences = FakeUserPreferencesRepository()
    private val interceptions = FakeInterceptionEventRepository()
    private var installMillis: Long? = Instant.parse("2026-09-01T08:00:00Z").toEpochMilli()
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: TreeRepositoryImpl

    // Viernes 16: la semana empezó el lunes 12
    private val today = LocalDate.of(2026, 10, 16)

    @Before
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = scope.backgroundScope,
            produceFile = { File(tmp.root, "tree.preferences_pb") }
        )
        repository = TreeRepositoryImpl(dataStore, profiles, preferences, interceptions, { installMillis }, clock)
    }

    private fun event(id: Long, time: String, result: InterceptionResult?) =
        InterceptionEvent(id, "com.example.video", Instant.parse(time).toEpochMilli(), result)

    @Test
    fun `las flores salen de las metas del perfil`() = scope.runTest {
        profiles.saveProfile(UserProfile(goals = listOf(Goal.SLEEP, Goal.FOCUS)))

        assertEquals(listOf(Goal.SLEEP, Goal.FOCUS), repository.observeTree(today).first().goals)
    }

    @Test
    fun `las hojas son las pausas respetadas desde el lunes`() = scope.runTest {
        interceptions.events.value = listOf(
            event(1, "2026-10-12T09:00:00Z", InterceptionResult.VOLVIO),
            event(2, "2026-10-15T09:00:00Z", InterceptionResult.VOLVIO),
            event(3, "2026-10-15T10:00:00Z", InterceptionResult.CONTINUO),
            event(4, "2026-10-11T23:00:00Z", InterceptionResult.VOLVIO)
        )

        assertEquals(2, repository.observeTree(today).first().weekLeaves)
    }

    @Test
    fun `las hojas vistas se guardan con su semana`() = scope.runTest {
        repository.markLeavesSeen(today, 3)

        assertEquals(3, repository.observeTree(today).first().seenLeaves)
        assertEquals(0, repository.observeTree(today.plusDays(4)).first().seenLeaves)
    }

    @Test
    fun `la fecha de plantado es la del onboarding`() = scope.runTest {
        preferences.markPlanted(Instant.parse("2026-10-14T18:00:00Z").toEpochMilli())

        assertEquals(LocalDate.of(2026, 10, 14), repository.observeTree(today).first().plantedAt)
    }

    @Test
    fun `sin fecha guardada se usa la de instalacion`() = scope.runTest {
        assertEquals(LocalDate.of(2026, 9, 1), repository.observeTree(today).first().plantedAt)
    }

    @Test
    fun `sin fecha guardada ni de instalacion no hay fecha`() = scope.runTest {
        installMillis = null

        assertEquals(null, repository.observeTree(today).first().plantedAt)
    }
}
