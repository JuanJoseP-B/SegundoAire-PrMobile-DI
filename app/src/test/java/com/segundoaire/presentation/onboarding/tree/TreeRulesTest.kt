package com.segundoaire.presentation.onboarding.tree

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TreeRulesTest {

    // Etapa por paso

    @Test
    fun `la etapa crece con los tres primeros pasos y se queda en cuatro`() {
        assertEquals(listOf(0, 1, 2, 3, 4, 4, 4), listOf(0, 1, 2, 3, 4, 7, 13).map(TreeRules::stage))
    }

    @Test
    fun `la bienvenida es la capa cero y cada pregunta la siguiente`() {
        assertEquals(0, TreeRules.layerIndex(0))
        assertEquals(0, TreeRules.layerIndex(2))
        assertEquals(1, TreeRules.layerIndex(3))
        assertEquals(10, TreeRules.layerIndex(12))
    }

    // Posición del árbol

    @Test
    fun `en la bienvenida el arbol sube con escala 1_55 y el suelo en 470`() {
        val pose = TreeRules.pose(0)

        assertEquals(1.55f, pose.scale, 0f)
        assertEquals(0.75f, pose.opacity, 0f)
        assertEquals(470f - 819f, pose.translateY, 0f)
        assertEquals(456f, pose.hillY, 0f)
    }

    @Test
    fun `el compromiso levanta el arbol y la colina lo sigue`() {
        val pose = TreeRules.pose(10)

        assertEquals(1.15f, pose.scale, 0f)
        assertEquals(-210f, pose.translateY, 0f)
        assertEquals(0.5f, pose.opacity, 0f)
        assertEquals(819f - 210f - 14f, pose.hillY, 0f)
    }

    @Test
    fun `el arbol se desvanece detras de la revelacion y desaparece con el telon`() {
        assertEquals(0.12f, TreeRules.pose(7).opacity, 0f)
        assertEquals(0.1f, TreeRules.pose(8).opacity, 0f)
        assertEquals(0.2f, TreeRules.pose(9).opacity, 0f)
        assertEquals(0f, TreeRules.pose(12).opacity, 0f)
        assertEquals(0f, TreeRules.pose(12).hillOpacity, 0f)
    }

    @Test
    fun `al pasar de la bienvenida el arbol se corre un diez por ciento`() {
        assertEquals(0f, TreeRules.pose(2).translateX, 0f)
        assertEquals(-39f, TreeRules.pose(3).translateX, 0.001f)
        assertEquals(-39f, TreeRules.pose(9).translateX, 0.001f)
    }

    // Marchitado por rango

    @Test
    fun `el follaje se marchita segun el rango de uso, solo en el paso de uso`() {
        assertEquals(listOf(0, 2, 5, 8, 12), (0..4).map { TreeRules.wiltCount(5, it) })
        assertEquals(0, TreeRules.wiltCount(4, 4))
        assertEquals(0, TreeRules.wiltCount(6, 4))
    }

    @Test
    fun `con doce marchitos se doran doce de los diecinueve circulos`() {
        val wilted = (0 until 19).count { TreeRules.isWilted(it, 12) }

        assertEquals(12, wilted)
    }

    @Test
    fun `sin marchitos no se dora ninguno`() {
        assertEquals(0, (0 until 19).count { TreeRules.isWilted(it, 0) })
    }

    // Hojas por nombre

    @Test
    fun `cada letra del nombre es una hoja, hasta diez, desde el paso del nombre`() {
        assertEquals(0, TreeRules.nameLeafCount(2, 5))
        assertEquals(5, TreeRules.nameLeafCount(3, 5))
        assertEquals(10, TreeRules.nameLeafCount(3, 25))
        assertEquals(0, TreeRules.nameLeafCount(3, 0))
    }

    // Flores

    @Test
    fun `una meta elegida hace florecer su flor desde el paso de objetivo`() {
        assertTrue(TreeRules.flowerOn(4, 2, setOf(2), committed = false))
        assertFalse(TreeRules.flowerOn(4, 1, setOf(2), committed = false))
        assertFalse(TreeRules.flowerOn(3, 2, setOf(2), committed = false))
    }

    @Test
    fun `al completar el compromiso florecen todas`() {
        assertTrue((0..4).all { TreeRules.flowerOn(10, it, emptySet(), committed = true) })
        assertFalse(TreeRules.flowerOn(9, 0, emptySet(), committed = true))
    }

    @Test
    fun `las flores se abren escalonadas y se cierran juntas`() {
        assertEquals(180, TreeRules.flowerDelayMillis(on = true, index = 2))
        assertEquals(0, TreeRules.flowerDelayMillis(on = false, index = 2))
    }

    // Raíces

    @Test
    fun `las raices se dibujan con el progreso del hold, escalonadas`() {
        assertEquals(1f, TreeRules.rootProgress(10, 1f, 0), 0f)
        assertEquals(0f, TreeRules.rootProgress(10, 0f, 0), 0f)
        assertEquals(0.75f - 0.06f * 2, TreeRules.rootProgress(10, 0.6f, 2), 0.0001f)
    }

    @Test
    fun `pasado el compromiso las raices quedan dibujadas y antes no hay ninguna`() {
        assertEquals(1f, TreeRules.rootProgress(11, 0f, 4), 0f)
        assertEquals(0f, TreeRules.rootProgress(9, 1f, 0), 0f)
        assertTrue(TreeRules.rootsVisible(10))
        assertTrue(TreeRules.rootsVisible(11))
        assertFalse(TreeRules.rootsVisible(12))
    }

    // Enredadera

    @Test
    fun `la enredadera se llena de paso menos dos entre nueve en los pasos 3 a 11`() {
        assertEquals(0f, TreeRules.vineProgress(2), 0f)
        assertEquals(1f / 9f, TreeRules.vineProgress(3), 0.0001f)
        assertEquals(1f, TreeRules.vineProgress(11), 0.0001f)
        assertEquals(1f, TreeRules.vineProgress(12), 0f)
    }

    // Caída de hojas y sacudida

    @Test
    fun `la hoja nace invisible arriba y termina invisible abajo`() {
        val start = TreeRules.fallPose(0f)
        val end = TreeRules.fallPose(1f)

        assertEquals(-30f, start.y, 0.001f)
        assertEquals(0f, start.alpha, 0.001f)
        assertEquals(720f, end.y, 0.001f)
        assertEquals(340f, end.rotation, 0.001f)
        assertEquals(0f, end.alpha, 0.001f)
    }

    @Test
    fun `a mitad de caida la hoja esta en el punto intermedio del prototipo`() {
        val mid = TreeRules.fallPose(0.5f)

        assertEquals(40f, mid.x, 0.001f)
        assertEquals(300f, mid.y, 0.001f)
        assertEquals(160f, mid.rotation, 0.001f)
    }

    @Test
    fun `la sacudida empieza y termina en cero y llega a seis a la izquierda`() {
        assertEquals(0f, TreeRules.shakeOffset(0f), 0.001f)
        assertEquals(-6f, TreeRules.shakeOffset(0.2f), 0.001f)
        assertEquals(5f, TreeRules.shakeOffset(0.4f), 0.001f)
        assertEquals(0f, TreeRules.shakeOffset(1f), 0.001f)
    }
}
