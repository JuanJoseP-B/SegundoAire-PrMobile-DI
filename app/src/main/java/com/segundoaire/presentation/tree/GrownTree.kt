package com.segundoaire.presentation.tree

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.segundoaire.presentation.designsystem.theme.SegundoAireColors

private const val Epsilon = 0.002f
private const val FlowerScale = 1.3f
private const val SlotLeafScale = 1.6f

/** Primera etapa del árbol ya crecido: el brote de la etapa 1 queda dentro del tronco. */
private const val GrownStage = 2

private val PetalCenters = arrayOf(
    Offset(0f, -8f), Offset(7.6f, -2.5f), Offset(4.7f, 6.5f), Offset(-4.7f, 6.5f), Offset(-7.6f, -2.5f)
)

/**
 * El árbol completo fuera del onboarding (Panel, Perfil e imagen para compartir), en
 * coordenadas del SVG del prototipo: tronco y ramas en `trunk`, follaje en `tree1..3`,
 * una flor por cada índice de [flowers] (posiciones de las opciones de Objetivo) y las
 * hojas de la semana en `leaf`. [leafScale] da la escala (0–1) de la hoja del hueco [Int];
 * [sway] es el balanceo en grados sobre la base del tronco.
 */
internal fun DrawScope.drawGrownTree(
    paths: TreePaths,
    colors: SegundoAireColors,
    flowers: List<Int>,
    leafCount: Int,
    leafScale: (Int) -> Float = { 1f },
    sway: Float = 0f
) {
    rotate(sway, TreeGeometry.SwayPivot) {
        TreeGeometry.Segments.forEachIndexed { i, segment ->
            if (segment.stage >= GrownStage) {
                drawPath(
                    path = paths.segments[i],
                    color = colors.trunk,
                    style = Stroke(width = segment.width, cap = StrokeCap.Round)
                )
            }
        }
        TreeGeometry.Blobs.forEach { blob ->
            val color = when (blob.colorIndex) {
                0 -> colors.tree1
                1 -> colors.tree2
                else -> colors.tree3
            }
            drawCircle(color, blob.r, Offset(blob.cx, blob.cy))
        }
        flowers.forEach { index ->
            val position = TreeGeometry.FlowerPositions.getOrNull(index) ?: return@forEach
            translate(position.x, position.y) {
                scale(FlowerScale, Offset.Zero) { drawFlower(colors.petal, colors.gold) }
            }
        }
        TreeGeometry.LeafSlots.take(leafCount).forEachIndexed { i, slot ->
            val s = leafScale(i)
            if (s > Epsilon) {
                translate(slot.x, slot.y) {
                    rotate(slot.angle, Offset.Zero) {
                        // La hoja brota desde su base (0 %, 50 %), como en la referencia
                        scale(SlotLeafScale * s, Offset.Zero) { drawPath(paths.leaf, colors.leaf) }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawFlower(petal: Color, center: Color) {
    PetalCenters.forEach { drawCircle(petal, 7f, it) }
    drawCircle(center, 4.5f, Offset.Zero)
}
