package com.segundoaire.data.repository

import com.segundoaire.data.local.dao.BlockSessionDao
import com.segundoaire.data.local.dao.InterceptionEventDao
import com.segundoaire.data.local.entity.BlockSessionEntity
import com.segundoaire.data.local.entity.InterceptionEventEntity
import com.segundoaire.domain.model.BlockSession
import com.segundoaire.domain.model.InterceptionResult
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.usecase.GenerarDatosDemoUseCase
import com.segundoaire.testing.FakeUserPreferencesRepository
import com.segundoaire.testing.MutableClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** El modo demo solo cambia lo que se lee: lo real se sigue guardando y vuelve intacto. */
class DemoModeRepositoriesTest {

    private val clock = MutableClock(Instant.parse("2026-10-13T23:00:00Z"))
    private val preferences = FakeUserPreferencesRepository()
    private val generateDemo = GenerarDatosDemoUseCase(clock)
    private val sessionDao = FakeBlockSessionDao()
    private val eventDao = FakeInterceptionEventDao()
    private val sessions = BlockSessionRepositoryImpl(sessionDao, preferences, generateDemo)
    private val events = InterceptionEventRepositoryImpl(eventDao, preferences, generateDemo)

    private val realSession = BlockSession(
        startedAt = clock.millis() - 20 * 60_000L,
        endedAt = clock.millis() - 5 * 60_000L,
        durationMinutes = 15,
        rigor = RigorLevel.STRICT,
        completed = true
    )

    @Test
    fun `con el demo apagado se lee el historial real`() = runTest {
        sessions.record(realSession)
        val id = events.record("com.instagram.android", clock.millis())
        events.setResult(id, InterceptionResult.VOLVIO)

        assertEquals(listOf(realSession), sessions.observeCompletedSince(0L).first())
        assertEquals(InterceptionResult.VOLVIO, events.observeSince(0L).first().single().result)
    }

    @Test
    fun `con el demo activo se leen los datos de ejemplo`() = runTest {
        sessions.record(realSession)
        events.record("com.instagram.android", clock.millis())

        preferences.setDemoMode(true)

        val demo = generateDemo()
        assertEquals(demo.sessions.filter { it.completed }, sessions.observeCompletedSince(0L).first())
        assertEquals(demo.events, events.observeSince(0L).first())
    }

    @Test
    fun `el demo filtra por fecha igual que la consulta real`() = runTest {
        preferences.setDemoMode(true)
        val from = Instant.parse("2026-10-12T00:00:00Z").toEpochMilli()

        val read = sessions.observeCompletedSince(from).first()

        assertEquals(read.sortedBy { it.endedAt }, read)
        assertTrue(read.isNotEmpty() && read.all { it.endedAt >= from })
        assertTrue(events.observeSince(from).first().all { it.timestampMillis >= from })
    }

    @Test
    fun `lo que se guarda durante el demo va al historial real y nada mas`() = runTest {
        sessions.record(realSession)
        preferences.setDemoMode(true)

        val id = events.record("com.instagram.android", clock.millis())
        events.setResult(id, InterceptionResult.CONTINUO)
        // Mientras dura el demo, lo recién guardado no se mezcla con los datos de ejemplo
        assertEquals(generateDemo().events, events.observeSince(0L).first())

        preferences.setDemoMode(false)

        assertEquals(listOf(realSession), sessions.observeCompletedSince(0L).first())
        assertEquals(1, sessionDao.rows.value.size)
        assertEquals(listOf(InterceptionResult.CONTINUO.name), eventDao.rows.value.map { it.result })
    }

    @Test
    fun `al cambiar el modo la lectura abierta cambia de fuente`() = runTest {
        sessions.record(realSession)
        val read = sessions.observeCompletedSince(0L)
        assertEquals(1, read.first().size)

        preferences.setDemoMode(true)
        assertEquals(generateDemo().sessions.count { it.completed }, read.first().size)

        preferences.setDemoMode(false)
        assertEquals(listOf(realSession), read.first())
    }
}

/** La tabla `block_sessions` en memoria, con su índice único en `started_at`. */
private class FakeBlockSessionDao : BlockSessionDao {
    val rows = MutableStateFlow<List<BlockSessionEntity>>(emptyList())

    override suspend fun insert(entity: BlockSessionEntity) {
        if (rows.value.none { it.startedAt == entity.startedAt }) {
            rows.value = rows.value + entity.copy(id = rows.value.size + 1L)
        }
    }

    override fun observeCompletedSince(fromMillis: Long): Flow<List<BlockSessionEntity>> =
        rows.map { list -> list.filter { it.completed && it.endedAt >= fromMillis }.sortedBy { it.endedAt } }
}

/** La tabla `interception_events` en memoria, con ids autoincrementales. */
private class FakeInterceptionEventDao : InterceptionEventDao {
    val rows = MutableStateFlow<List<InterceptionEventEntity>>(emptyList())

    override suspend fun insert(entity: InterceptionEventEntity): Long {
        val id = rows.value.size + 1L
        rows.value = rows.value + entity.copy(id = id)
        return id
    }

    override suspend fun setResult(id: Long, result: String) {
        rows.value = rows.value.map { if (it.id == id) it.copy(result = result) else it }
    }

    override suspend fun setIntent(id: Long, intent: String) {
        rows.value = rows.value.map { if (it.id == id) it.copy(intent = intent) else it }
    }

    override fun observeSince(fromMillis: Long): Flow<List<InterceptionEventEntity>> =
        rows.map { list -> list.filter { it.timestampMillis >= fromMillis }.sortedBy { it.timestampMillis } }
}
