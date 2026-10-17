package com.segundoaire.presentation.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pares críticos de DESIGN_SYSTEM.md §3.3, medidos con la fórmula de contraste de WCAG 2. */
class ColorContrastTest {

    private val light = LightSegundoAireColors
    private val dark = DarkSegundoAireColors

    @Test
    fun `oscuro - primary sobre background cumple texto`() =
        assertText(dark.primary, dark.background)

    @Test
    fun `oscuro - onPrimary sobre primary cumple texto`() =
        assertText(dark.onPrimary, dark.primary)

    @Test
    fun `oscuro - onBackground sobre secondary cumple texto`() =
        assertText(dark.onBackground, dark.secondary)

    @Test
    fun `claro - onBackground sobre surfaceGlass cumple texto`() =
        assertText(light.onBackground, light.surfaceGlass)

    @Test
    fun `claro - onBackgroundMuted sobre glass3 cumple texto`() =
        assertText(light.onBackgroundMuted, light.glass3.compositeOver(light.background))

    @Test
    fun `claro - outline sobre background cumple borde`() =
        assertBorder(light.outline, light.background)

    @Test
    fun `oscuro - outline sobre background cumple borde`() =
        assertBorder(dark.outline, dark.background)

    @Test
    fun `claro - primary sobre background cumple texto`() =
        assertText(light.primary, light.background)

    @Test
    fun `claro - onPrimary sobre primary cumple texto`() =
        assertText(light.onPrimary, light.primary)

    @Test
    fun `claro - onBackgroundMuted sobre surfaceGlass cumple texto`() =
        assertText(light.onBackgroundMuted, light.surfaceGlass)

    @Test
    fun `texto sobre cada nivel de vidrio cumple en ambos temas`() {
        listOf(light, dark).forEach { colors ->
            listOf(colors.glass1, colors.glass2, colors.glass3).forEach { glass ->
                val surface = glass.compositeOver(colors.background)
                assertText(colors.onBackground, surface)
                assertText(colors.onBackgroundMuted, surface)
            }
        }
    }

    @Test
    fun `el vidrio sube de opacidad con el nivel en ambos temas`() {
        listOf(light, dark).forEach { colors ->
            assertTrue(colors.glass1.alpha < colors.glass2.alpha)
            assertTrue(colors.glass2.alpha < colors.glass3.alpha)
        }
    }

    @Test
    fun `texto principal y secundario cumplen sobre background en ambos temas`() {
        listOf(light, dark).forEach { colors ->
            assertText(colors.onBackground, colors.background)
            assertText(colors.onBackgroundMuted, colors.background)
            assertText(colors.onPrimary, colors.primary)
        }
    }

    private fun assertText(foreground: Color, background: Color) =
        assertContrast(foreground, background, TextMinimum)

    private fun assertBorder(foreground: Color, background: Color) =
        assertContrast(foreground, background, BorderMinimum)

    private fun assertContrast(foreground: Color, background: Color, minimum: Float) {
        val ratio = contrastRatio(foreground, background)
        assertTrue("Contraste $ratio, por debajo de $minimum", ratio >= minimum)
    }

    private fun contrastRatio(a: Color, b: Color): Float {
        val lighter = maxOf(a.luminance(), b.luminance())
        val darker = minOf(a.luminance(), b.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    private companion object {
        const val TextMinimum = 4.5f
        const val BorderMinimum = 3f
    }
}
