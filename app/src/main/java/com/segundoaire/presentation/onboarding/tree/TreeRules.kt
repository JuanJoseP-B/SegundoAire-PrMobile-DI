package com.segundoaire.presentation.onboarding.tree

import com.segundoaire.presentation.designsystem.theme.Motion
import com.segundoaire.presentation.tree.TreeGeometry
import kotlin.math.max
import kotlin.math.min

/** Dónde se pone el árbol y la colina en un paso. Unidades del marco de 390 × 844. */
internal data class TreePose(
    val translateX: Float,
    val translateY: Float,
    val scale: Float,
    val opacity: Float,
    val hillY: Float,
    val hillOpacity: Float
)

/** Dónde está una hoja o un pétalo que cae, a lo largo de su recorrido. */
internal data class FallPose(val x: Float, val y: Float, val rotation: Float, val alpha: Float)

/**
 * Reglas del árbol sin dibujo: qué crece en cada paso, qué se marchita, cuándo florece.
 * Salen del prototipo aprobado y están aparte para poder probarlas.
 */
internal object TreeRules {
    const val FrameWidth = 390f
    const val FrameHeight = 844f

    /** Altura del suelo en la bienvenida (el árbol sube con la escala 1.55). */
    const val WelcomeGround = 470f

    // Donde cae el suelo del árbol con escala 1: arriba del área (564) más 255
    private const val GroundAtRest = 819f

    private val WiltByUsage = intArrayOf(0, 2, 5, 8, 12)
    private const val WiltModulus = 19
    private const val WiltStride = 7

    const val CommitStep = 10
    const val WelcomeLastStep = 2
    const val NameStep = 3
    const val GoalsStep = 4
    const val UsageStep = 5

    /** Etapa de crecimiento: 0 semilla, 1 brote, 2 tronco y primeras ramas, 3 copa, 4 copa completa. */
    fun stage(step: Int): Int = step.coerceIn(0, 4)

    /** Capa activa: la bienvenida entera es la capa 0 y cada pregunta, la siguiente. */
    fun layerIndex(step: Int): Int = if (step <= WelcomeLastStep) 0 else step - 2

    fun pose(step: Int): TreePose {
        val welcome = step <= WelcomeLastStep
        var translateY = 0f
        var scale = 1f
        var opacity = 0.32f
        if (welcome) {
            translateY = WelcomeGround - GroundAtRest
            scale = 1.55f
            opacity = 0.75f
        }
        when (step) {
            7 -> opacity = 0.12f
            8 -> opacity = 0.1f
            9 -> opacity = 0.2f
            CommitStep -> {
                translateY = -210f
                scale = 1.15f
                opacity = 0.5f
            }
            11 -> opacity = 0.25f
        }
        if (step >= 12) opacity = 0f

        val hillY = when {
            welcome -> WelcomeGround - 14f
            step == CommitStep -> GroundAtRest + translateY - 14f
            else -> 805f
        }
        return TreePose(
            translateX = -min(layerIndex(step), 1) * FrameWidth * 0.1f,
            translateY = translateY,
            scale = scale,
            opacity = opacity,
            hillY = hillY,
            hillOpacity = if (step >= 12) 0f else 1f
        )
    }

    /** Hojas marchitas del follaje según el rango de uso (0 = menos de 2 h … 4 = más de 8 h). */
    fun wiltCount(step: Int, usageIndex: Int): Int =
        if (step == UsageStep) WiltByUsage[usageIndex.coerceIn(0, WiltByUsage.lastIndex)] else 0

    /** Reparte los círculos marchitos de forma pareja entre todo el follaje. */
    fun isWilted(blobIndex: Int, wiltCount: Int): Boolean = (blobIndex * WiltStride) % WiltModulus < wiltCount

    /** Hojas con el nombre: una por letra, hasta 10, desde el paso del nombre. */
    fun nameLeafCount(step: Int, nameLength: Int): Int =
        if (step >= NameStep) nameLength.coerceIn(0, TreeGeometry.NameLeafMax) else 0

    fun flowerOn(step: Int, index: Int, picks: Set<Int>, committed: Boolean): Boolean =
        (step >= GoalsStep && index in picks) || (committed && step >= CommitStep)

    fun flowerDelayMillis(on: Boolean, index: Int): Int = if (on) index * 90 else 0

    /** La copa crece escalonada solo mientras se recorre la bienvenida y el primer paso. */
    fun growDelayMillis(on: Boolean, step: Int, baseMillis: Int, order: Int): Int =
        if (on && step <= GoalsStep) baseMillis + order * 90 else 0

    /** Avance de la raíz [index] según el progreso de mantener presionado. */
    fun rootProgress(step: Int, hold: Float, index: Int): Float {
        val base = when {
            step == CommitStep -> hold
            step > CommitStep -> 1f
            else -> 0f
        }
        return (base * 1.25f - index * 0.06f).coerceIn(0f, 1f)
    }

    fun rootsVisible(step: Int): Boolean = step in CommitStep..11

    /** Avance de la enredadera: (paso − 2) / 9 en los pasos 3–11 y llena al salir de ellos. */
    fun vineProgress(step: Int): Float = when {
        step in NameStep..11 -> (step - 2) / 9f
        step > 11 -> 1f
        else -> 0f
    }

    /** Vaivén de las hojas que caen a lo largo de su recorrido (0 al inicio, 1 al final). */
    fun fallPose(t: Float): FallPose {
        val time = t.coerceIn(0f, 1f)
        val x: Float
        val y: Float
        val rotation: Float
        if (time <= 0.5f) {
            val f = Motion.GustEasing.transform(time / 0.5f)
            x = lerp(0f, 40f, f)
            y = lerp(-30f, 300f, f)
            rotation = lerp(0f, 160f, f)
        } else {
            val f = Motion.GustEasing.transform((time - 0.5f) / 0.5f)
            x = lerp(40f, -10f, f)
            y = lerp(300f, 720f, f)
            rotation = lerp(160f, 340f, f)
        }
        val alpha = if (time <= 0.15f) {
            lerp(0f, 0.5f, Motion.GustEasing.transform(time / 0.15f))
        } else {
            lerp(0.5f, 0f, Motion.GustEasing.transform((time - 0.15f) / 0.85f))
        }
        return FallPose(x, y, rotation, alpha)
    }

    /** Desplazamiento horizontal de la sacudida (en dp) para el avance [t] de 0 a 1. */
    fun shakeOffset(t: Float): Float {
        val time = t.coerceIn(0f, 1f)
        val keys = floatArrayOf(0f, -6f, 5f, -3f, 2f, 0f)
        val at = min((time / 0.2f).toInt(), keys.lastIndex - 1)
        val f = Motion.ShakeEasing.transform(max(0f, min(1f, time / 0.2f - at)))
        return lerp(keys[at], keys[at + 1], f)
    }

    private fun lerp(a: Float, b: Float, f: Float): Float = a + (b - a) * f
}
