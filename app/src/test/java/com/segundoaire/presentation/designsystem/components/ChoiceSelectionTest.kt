package com.segundoaire.presentation.designsystem.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Reglas de selección de ChoiceChipGroup: única, múltiple y múltiple con límite. */
class ChoiceSelectionTest {

    @Test
    fun `unica - elegir otro chip reemplaza al anterior`() =
        assertEquals(setOf(2), toggleChoice(selected = setOf(0), index = 2, maxSelected = 1))

    @Test
    fun `unica - tocar el chip elegido lo mantiene`() =
        assertEquals(setOf(0), toggleChoice(selected = setOf(0), index = 0, maxSelected = 1))

    @Test
    fun `multiple - tocar un chip elegido lo quita`() =
        assertEquals(setOf(1), toggleChoice(selected = setOf(0, 1), index = 0, maxSelected = 2))

    @Test
    fun `multiple - bajo el limite agrega el chip`() =
        assertEquals(setOf(0, 3), toggleChoice(selected = setOf(0), index = 3, maxSelected = 2))

    @Test
    fun `multiple - en el limite no agrega mas`() =
        assertEquals(setOf(0, 1), toggleChoice(selected = setOf(0, 1), index = 3, maxSelected = 2))

    @Test
    fun `en el limite solo se pueden tocar los chips elegidos`() {
        assertFalse(canToggleChoice(selected = setOf(0, 1), index = 3, maxSelected = 2))
        assertTrue(canToggleChoice(selected = setOf(0, 1), index = 1, maxSelected = 2))
    }

    @Test
    fun `unica - todos los chips se pueden tocar siempre`() =
        assertTrue(canToggleChoice(selected = setOf(0), index = 3, maxSelected = 1))
}
