package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.ActiveBlock
import com.segundoaire.domain.model.AppLanguage
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.testing.FakeAppLanguageRepository
import com.segundoaire.testing.FakeAppResetRepository
import com.segundoaire.testing.FakeBlockRepository
import com.segundoaire.testing.MutableClock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class ResetAppUseCaseTest {

    private val clock = MutableClock()
    private val appReset = FakeAppResetRepository()

    private fun blocks(active: ActiveBlock? = null) = FakeBlockRepository(clock, initial = active)

    @Test
    fun `borra los datos locales una vez`() = runTest {
        ResetAppUseCase(appReset, blocks(), FakeAppLanguageRepository())()

        assertEquals(1, appReset.clearCount)
    }

    @Test
    fun `vuelve al idioma del sistema`() = runTest {
        val languages = FakeAppLanguageRepository(AppLanguage.ENGLISH)

        ResetAppUseCase(appReset, blocks(), languages)()

        assertEquals(AppLanguage.SYSTEM, languages.current())
    }

    @Test
    fun `si ya sigue al sistema no vuelve a aplicar el idioma`() = runTest {
        // Aplicar un idioma recrea la actividad: hacerlo sin cambio sería un parpadeo inútil
        val languages = FakeAppLanguageRepository(AppLanguage.SYSTEM)

        ResetAppUseCase(appReset, blocks(), languages)()

        assertEquals(0, languages.setCount)
    }

    @Test
    fun `un bloque Strict en curso no sobrevive al reinicio`() = runTest {
        val strict = ActiveBlock.starting(clock.millis(), 30, RigorLevel.STRICT)
        val blocks = blocks(active = strict)

        ResetAppUseCase(appReset, blocks, FakeAppLanguageRepository())()

        assertNull(blocks.observeActiveBlock().value)
        // Se olvida sin pasar por el historial, que el reinicio también borra
        assertEquals(0, blocks.stopCount)
    }

    @Test
    fun `si no se puede borrar no se toca el idioma ni el bloque`() = runTest {
        val strict = ActiveBlock.starting(clock.millis(), 30, RigorLevel.STRICT)
        val blocks = blocks(active = strict)
        val languages = FakeAppLanguageRepository(AppLanguage.SPANISH)
        appReset.failNext = true

        try {
            ResetAppUseCase(appReset, blocks, languages)()
            fail("Debía propagar el error de escritura")
        } catch (_: IOException) {
            // esperado: la pantalla lo muestra y deja reintentar
        }

        assertEquals(AppLanguage.SPANISH, languages.current())
        assertEquals(strict, blocks.observeActiveBlock().value)
    }
}
