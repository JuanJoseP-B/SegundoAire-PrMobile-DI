package com.segundoaire.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.segundoaire.domain.model.ActiveBlock
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.testing.FakeBlockSessionRepository
import com.segundoaire.testing.MutableClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.Duration

class BlockRepositoryImplTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val scope = TestScope(UnconfinedTestDispatcher())
    private val clock = MutableClock()
    private val sessions = FakeBlockSessionRepository()
    private lateinit var dataStore: DataStore<Preferences>

    @Before
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = scope.backgroundScope,
            produceFile = { File(tmp.root, "blocks.preferences_pb") }
        )
    }

    // Cada instancia equivale a un arranque de la app sobre los mismos datos
    private fun repository() = BlockRepositoryImpl(dataStore, clock, sessions, scope.backgroundScope)

    private suspend fun BlockRepositoryImpl.awaitBlock(): ActiveBlock =
        observeActiveBlock().first { it != null }!!

    private suspend fun BlockRepositoryImpl.awaitCompletedMinutes(): Int =
        observeCompletedMinutes().first { it != null }!!

    private suspend fun awaitNothingStored() {
        dataStore.data.first { prefs -> prefs.asMap().keys.none { it.name.startsWith("block_") } }
    }

    @Test
    fun `sin datos guardados no hay bloque activo`() = scope.runTest {
        val repository = repository()
        repository.stop()

        assertNull(repository.observeActiveBlock().value)
    }

    @Test
    fun `iniciar calcula el fin desde el reloj y la duracion`() = scope.runTest {
        val repository = repository()
        val now = clock.millis()

        repository.start(durationMinutes = 15, rigor = RigorLevel.STRICT)

        assertEquals(
            ActiveBlock(
                startedAt = now,
                endsAt = now + 15 * 60_000L,
                rigor = RigorLevel.STRICT,
                durationMinutes = 15
            ),
            repository.observeActiveBlock().value
        )
    }

    @Test
    fun `el bloque activo sobrevive al cierre de la app`() = scope.runTest {
        repository().start(durationMinutes = 30, rigor = RigorLevel.MINDFUL)
        val started = clock.millis()
        clock.instant += Duration.ofMinutes(10)

        val restored = repository().awaitBlock()

        assertEquals(ActiveBlock.starting(started, 30, RigorLevel.MINDFUL), restored)
        assertEquals(20 * 60_000L, restored.remainingMillis(clock.millis()))
    }

    @Test
    fun `terminar el bloque lo borra tambien del disco`() = scope.runTest {
        val repository = repository()
        repository.start(durationMinutes = 5, rigor = RigorLevel.MINDFUL)

        repository.stop()

        assertNull(repository.observeActiveBlock().value)
        awaitNothingStored()
        val reopened = repository()
        reopened.stop()
        assertNull(reopened.observeActiveBlock().value)
    }

    @Test
    fun `completar el bloque lo deja sin bloque activo y como completado por ver`() = scope.runTest {
        val repository = repository()
        repository.start(durationMinutes = 5, rigor = RigorLevel.STRICT)

        repository.complete()

        assertNull(repository.observeActiveBlock().value)
        assertEquals(5, repository.observeCompletedMinutes().value)
    }

    @Test
    fun `terminar antes de tiempo no cuenta como completado`() = scope.runTest {
        val repository = repository()
        repository.start(durationMinutes = 5, rigor = RigorLevel.STRICT)

        repository.stop()

        assertNull(repository.observeCompletedMinutes().value)
    }

    @Test
    fun `el completado por ver sobrevive al cierre de la app`() = scope.runTest {
        val repository = repository()
        repository.start(durationMinutes = 30, rigor = RigorLevel.MINDFUL)
        repository.complete()

        val reopened = repository()

        assertEquals(30, reopened.awaitCompletedMinutes())
        assertNull(reopened.observeActiveBlock().value)
    }

    @Test
    fun `un bloque que vencio con la app cerrada vuelve como completado`() = scope.runTest {
        repository().start(durationMinutes = 5, rigor = RigorLevel.STRICT)
        clock.instant += Duration.ofMinutes(5)

        val reopened = repository()

        assertEquals(5, reopened.awaitCompletedMinutes())
        assertNull(reopened.observeActiveBlock().value)
    }

    @Test
    fun `al verlo el completado se borra tambien del disco`() = scope.runTest {
        val repository = repository()
        repository.start(durationMinutes = 5, rigor = RigorLevel.STRICT)
        repository.complete()

        repository.acknowledgeCompleted()

        assertNull(repository.observeCompletedMinutes().value)
        awaitNothingStored()
    }

    @Test
    fun `iniciar otro bloque descarta el completado que no se vio`() = scope.runTest {
        val repository = repository()
        repository.start(durationMinutes = 5, rigor = RigorLevel.STRICT)
        repository.complete()

        repository.start(durationMinutes = 10, rigor = RigorLevel.MINDFUL)

        assertNull(repository.observeCompletedMinutes().value)
        repository.stop()
        awaitNothingStored()
    }

    @Test
    fun `iniciar otro bloque reemplaza al anterior`() = scope.runTest {
        val repository = repository()
        repository.start(durationMinutes = 5, rigor = RigorLevel.MINDFUL)
        clock.instant += Duration.ofMinutes(1)

        repository.start(durationMinutes = 10, rigor = RigorLevel.STRICT)

        assertEquals(
            ActiveBlock.starting(clock.millis(), 10, RigorLevel.STRICT),
            repository.observeActiveBlock().value
        )
    }

    @Test
    fun `datos corruptos se descartan en vez de fallar`() = scope.runTest {
        dataStore.edit { prefs ->
            prefs[longPreferencesKey("block_started_at")] = clock.millis()
            prefs[longPreferencesKey("block_ends_at")] = clock.millis() + 60_000L
            prefs[stringPreferencesKey("block_rigor")] = "EXTREMO"
        }

        val repository = repository()

        awaitNothingStored()
        assertNull(repository.observeActiveBlock().value)
    }

    @Test
    fun `el bloque no toca el perfil guardado en el mismo DataStore`() = scope.runTest {
        val profiles = UserProfileRepositoryImpl(dataStore)
        profiles.saveProfile(UserProfile(name = "Ana"))
        val repository = repository()

        repository.start(durationMinutes = 5, rigor = RigorLevel.MINDFUL)
        repository.stop()

        assertEquals(UserProfile(name = "Ana"), profiles.profile.first())
    }
}
