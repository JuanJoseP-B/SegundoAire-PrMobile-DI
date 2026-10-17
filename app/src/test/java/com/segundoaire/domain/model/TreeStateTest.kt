package com.segundoaire.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TreeStateTest {

    @Test
    fun `cada pausa respetada de la semana es una hoja, hasta ocho`() {
        assertEquals(0, TreeState(weekLeaves = 0).leafCount)
        assertEquals(5, TreeState(weekLeaves = 5).leafCount)
        assertEquals(TreeState.MAX_LEAVES, TreeState(weekLeaves = 12).leafCount)
    }

    @Test
    fun `solo brotan las hojas que no se vieron`() {
        val tree = TreeState(weekLeaves = 5, seenLeaves = 3)

        assertEquals(3, tree.shownLeaves)
        assertEquals(2, tree.newLeaves)
    }

    @Test
    fun `las vistas nunca superan las que hay`() {
        val tree = TreeState(weekLeaves = 2, seenLeaves = 6)

        assertEquals(2, tree.shownLeaves)
        assertEquals(0, tree.newLeaves)
    }

    @Test
    fun `las hojas vistas de otra semana no cuentan`() {
        val monday = LocalDate.of(2026, 10, 12)

        assertEquals(4, TreeState.seenFor(monday, 4, monday))
        assertEquals(0, TreeState.seenFor(monday.minusWeeks(1), 4, monday))
        assertEquals(0, TreeState.seenFor(null, 4, monday))
    }

    @Test
    fun `la semana empieza el lunes`() {
        val monday = LocalDate.of(2026, 10, 12)

        assertEquals(monday, monday.weekStart())
        assertEquals(monday, LocalDate.of(2026, 10, 16).weekStart())
        assertEquals(monday, LocalDate.of(2026, 10, 18).weekStart())
        assertEquals(monday.plusWeeks(1), LocalDate.of(2026, 10, 19).weekStart())
    }
}
