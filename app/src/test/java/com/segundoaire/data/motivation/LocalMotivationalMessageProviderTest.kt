package com.segundoaire.data.motivation

import com.segundoaire.domain.model.Goal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class LocalMotivationalMessageProviderTest {

    private val banks = mapOf(
        Goal.FOCUS to listOf("f1", "f2", "f3"),
        Goal.SLEEP to listOf("s1", "s2"),
        null to listOf("g1", "g2")
    )

    private fun provider(thinkingMillis: Long = 800, seed: Int = 7) = LocalMotivationalMessageProvider(
        bank = { goal -> banks.getValue(goal) },
        thinkingMillis = thinkingMillis,
        random = Random(seed)
    )

    @Test
    fun `devuelve un mensaje del banco del objetivo`() = runTest {
        val message = provider().nextMessage(Goal.SLEEP)

        assertTrue(message in banks.getValue(Goal.SLEEP))
    }

    @Test
    fun `sin objetivo usa el banco general`() = runTest {
        val message = provider().nextMessage(goal = null)

        assertTrue(message in banks.getValue(null))
    }

    @Test
    fun `nunca repite el mensaje anterior`() = runTest {
        repeat(50) { seed ->
            val message = provider(seed = seed).nextMessage(Goal.FOCUS, previous = "f2")

            assertNotEquals("f2", message)
        }
    }

    @Test
    fun `con un solo mensaje lo repite en vez de fallar`() {
        assertEquals("unico", pickMessage(listOf("unico"), previous = "unico", random = Random(1)))
    }

    @Test
    fun `simula el tiempo de pensar`() = runTest {
        provider(thinkingMillis = 800).nextMessage(Goal.FOCUS)

        assertEquals(800L, currentTime)
    }
}
