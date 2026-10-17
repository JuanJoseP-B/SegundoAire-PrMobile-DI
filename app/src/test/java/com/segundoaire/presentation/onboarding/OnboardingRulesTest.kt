package com.segundoaire.presentation.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class OnboardingRulesTest {

    private val spanish = Locale.forLanguageTag("es-CO")
    private val english = Locale.US

    // Años de la fórmula

    @Test
    fun `los años siguen la formula h por 365 por 50 entre 8760`() {
        assertEquals(1.5 * 365 * 50 / 8760, OnboardingRules.projectedYears(1.5), 1e-9)
        assertEquals(5 * 365 * 50 / 8760.0, OnboardingRules.projectedYears(5.0), 1e-9)
    }

    @Test
    fun `cada rango de uso representa las horas del prototipo`() {
        assertEquals(listOf(1.5, 3.0, 5.0, 7.0, 9.0), (0..4).map(OnboardingRules::usageHours))
    }

    @Test
    fun `recuperar una hora al dia son unos dos años`() {
        assertEquals("2,1", OnboardingRules.formatYears(OnboardingRules.recoveredYears(), spanish))
    }

    @Test
    fun `en español los decimales llevan coma y en ingles punto`() {
        assertEquals("10,4", OnboardingRules.formatYears(OnboardingRules.projectedYears(5.0), spanish))
        assertEquals("10.4", OnboardingRules.formatYears(OnboardingRules.projectedYears(5.0), english))
    }

    @Test
    fun `la formula se muestra con los separadores del idioma`() {
        assertEquals("1,5 h × 365 × 50 ÷ 8.760", OnboardingRules.formula(1.5, spanish))
        assertEquals("5 h × 365 × 50 ÷ 8,760", OnboardingRules.formula(5.0, english))
    }

    // Cascadas

    @Test
    fun `las opciones entran cada 60 ms desde los 80 ms`() {
        assertEquals(listOf(80, 140, 200, 260, 320), (0..4).map(OnboardingRules::cascadeDelayMillis))
    }

    @Test
    fun `las filas del plan entran cada 90 ms desde los 200 ms`() {
        assertEquals(listOf(200, 290, 380, 470), (0..3).map(OnboardingRules::planRowDelayMillis))
    }

    // Hold

    @Test
    fun `mantener presionado llena en un segundo y medio`() {
        var hold = 0f
        repeat(15) { hold = OnboardingRules.advanceHold(hold, holding = true, dtMillis = 100f) }

        assertEquals(1f, hold, 0.0001f)
    }

    @Test
    fun `al soltar se vacia en 300 ms`() {
        val afterRelease = OnboardingRules.advanceHold(1f, holding = false, dtMillis = 150f)

        assertEquals(0.5f, afterRelease, 0.0001f)
        assertEquals(0f, OnboardingRules.advanceHold(afterRelease, holding = false, dtMillis = 300f), 0f)
    }

    @Test
    fun `el hold no pasa de uno ni baja de cero`() {
        assertEquals(1f, OnboardingRules.advanceHold(0.99f, true, 500f), 0f)
        assertEquals(0f, OnboardingRules.advanceHold(0.01f, false, 500f), 0f)
    }

    @Test
    fun `las marcas se cruzan al 33 y al 66 por ciento`() {
        assertEquals(0, OnboardingRules.marksReached(0.32f))
        assertEquals(1, OnboardingRules.marksReached(0.33f))
        assertEquals(1, OnboardingRules.marksReached(0.65f))
        assertEquals(2, OnboardingRules.marksReached(0.66f))
        assertEquals(2, OnboardingRules.marksReached(1f))
    }

    // Límite de selección

    @Test
    fun `se pueden elegir dos metas y la tercera se rechaza`() {
        val first = OnboardingRules.togglePick(emptyList(), 3) as PickResult.Added
        val second = OnboardingRules.togglePick(first.picks, 0) as PickResult.Added

        assertEquals(listOf(3, 0), second.picks)
        assertEquals(PickResult.Denied, OnboardingRules.togglePick(second.picks, 4))
    }

    @Test
    fun `tocar una meta elegida la quita aunque ya haya dos`() {
        val result = OnboardingRules.togglePick(listOf(3, 0), 3)

        assertEquals(PickResult.Removed(listOf(0)), result)
    }

    // Títulos con palabra clave

    @Test
    fun `el texto entre asteriscos es la palabra clave`() {
        assertEquals(
            listOf("Respira " to false, "antes" to true, " de abrir" to false),
            OnboardingRules.splitEmphasis("Respira *antes* de abrir")
        )
    }

    @Test
    fun `un texto sin asteriscos queda entero y un asterisco suelto no rompe nada`() {
        assertEquals(listOf("Hola" to false), OnboardingRules.splitEmphasis("Hola"))
        assertEquals(listOf("2 * 3" to false), OnboardingRules.splitEmphasis("2 * 3"))
        assertTrue(OnboardingRules.splitEmphasis("").isEmpty())
    }

    @Test
    fun `la palabra clave puede abrir o cerrar el texto`() {
        assertEquals(
            listOf("Todo listo, " to false, "Ana" to true, "." to false),
            OnboardingRules.splitEmphasis("Todo listo, *Ana*.")
        )
        assertEquals(listOf("Recupera " to false, "tu tiempo" to true), OnboardingRules.splitEmphasis("Recupera *tu tiempo*"))
    }

    // Nombre

    @Test
    fun `las hojas del nombre llegan a diez`() {
        assertEquals(4, OnboardingRules.nameLeaves("Juan"))
        assertEquals(10, OnboardingRules.nameLeaves("Maria de los Angeles"))
    }
}
