package com.segundoaire.presentation.onboarding.tree

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

/** Rama del tronco o de la copa: se dibuja como un trazo que crece hasta su [stage]. */
internal class TreeSegmentDef(val d: String, val width: Float, val stage: Int, val delayMillis: Int)

internal class TreeRootDef(val d: String, val width: Float)

/** Círculo de follaje. [colorIndex] elige entre primary, glow y mint; [order] es su posición dentro de su etapa. */
internal class TreeBlobDef(
    val cx: Float,
    val cy: Float,
    val r: Float,
    val stage: Int,
    val colorIndex: Int,
    val order: Int
)

internal class TreeLeafDef(
    val x: Float,
    val y: Float,
    val angle: Float,
    val stage: Int,
    val scale: Float,
    val order: Int
)

internal class NameLeafDef(val x: Float, val y: Float, val angle: Float)

/**
 * Coordenadas del árbol, tal como están en el prototipo aprobado (SVG de 390 × 280
 * con `translate(195 255) scale(0.6) translate(-195 -515)`). Todo en unidades SVG.
 */
internal object TreeGeometry {
    const val AreaWidth = 390f
    const val AreaHeight = 280f

    /** Dónde empieza el área del árbol dentro del marco de 390 × 844. */
    const val AreaTop = 564f

    /** Origen del escalado de la capa del árbol: 50 % de ancho y 91 % de alto. */
    val LayerOrigin = Offset(AreaWidth * 0.5f, AreaHeight * 0.91f)

    /** Punto del SVG que queda en el suelo del área: `translate(195 255) … translate(-195 -515)`. */
    val GroupTranslate = Offset(195f, 255f)
    val GroupPivot = Offset(195f, 515f)
    const val GroupScale = 0.6f

    /** Punto sobre el que se balancea el árbol. */
    val SwayPivot = Offset(195f, 515f)

    const val NameLeafMax = 10

    val Segments: List<TreeSegmentDef> = listOf(
        TreeSegmentDef("M195 515 C195 490 193 470 196 440", 5f, 1, 0),
        TreeSegmentDef("M195 515 C190 450 202 380 195 300", 10f, 2, 0),
        TreeSegmentDef("M196 400 C215 385 235 380 255 362", 4.5f, 2, 400),
        TreeSegmentDef("M194 370 C175 355 158 345 140 328", 4.5f, 2, 480),
        TreeSegmentDef("M195 300 C190 250 200 210 195 165", 8f, 3, 0),
        TreeSegmentDef("M195 250 C220 235 245 228 278 210", 4.5f, 3, 350),
        TreeSegmentDef("M195 232 C170 218 150 208 115 196", 4.5f, 3, 420),
        TreeSegmentDef("M195 190 C212 160 232 140 262 126", 3.5f, 4, 0),
        TreeSegmentDef("M195 185 C178 155 158 140 128 124", 3.5f, 4, 80),
        TreeSegmentDef("M195 170 L195 110", 3.5f, 4, 120)
    )

    val Roots: List<TreeRootDef> = listOf(
        TreeRootDef("M195 515 C190 560 170 600 140 650", 5f),
        TreeRootDef("M195 515 C200 570 230 610 270 660", 5f),
        TreeRootDef("M195 515 C196 580 192 640 195 710", 6f),
        TreeRootDef("M182 560 C150 580 120 590 80 600", 3f),
        TreeRootDef("M210 572 C250 585 280 592 315 600", 3f)
    )

    private val BlobData = listOf(
        floatArrayOf(196f, 300f, 40f, 2f), floatArrayOf(255f, 352f, 32f, 2f), floatArrayOf(140f, 318f, 32f, 2f),
        floatArrayOf(228f, 318f, 28f, 2f), floatArrayOf(165f, 300f, 28f, 2f),
        floatArrayOf(195f, 170f, 52f, 3f), floatArrayOf(278f, 205f, 40f, 3f), floatArrayOf(115f, 192f, 40f, 3f),
        floatArrayOf(245f, 240f, 34f, 3f), floatArrayOf(148f, 232f, 34f, 3f), floatArrayOf(205f, 228f, 36f, 3f),
        floatArrayOf(195f, 100f, 56f, 4f), floatArrayOf(262f, 118f, 46f, 4f), floatArrayOf(128f, 116f, 46f, 4f),
        floatArrayOf(305f, 160f, 38f, 4f), floatArrayOf(85f, 155f, 38f, 4f), floatArrayOf(235f, 150f, 44f, 4f),
        floatArrayOf(155f, 148f, 44f, 4f), floatArrayOf(195f, 140f, 48f, 4f)
    )

    val Blobs: List<TreeBlobDef> = run {
        val perStage = HashMap<Int, Int>()
        BlobData.mapIndexed { i, b ->
            val stage = b[3].toInt()
            val order = (perStage[stage] ?: 0) + 1
            perStage[stage] = order
            TreeBlobDef(b[0], b[1], b[2], stage, colorIndex = i % 3, order = order)
        }
    }

    private val LeafData = listOf(
        floatArrayOf(196f, 442f, -150f, 1f, 1.5f), floatArrayOf(196f, 442f, -30f, 1f, 1.5f),
        floatArrayOf(196f, 452f, -160f, 1f, 1.1f), floatArrayOf(196f, 452f, -20f, 1f, 1.1f),
        floatArrayOf(300f, 330f, -15f, 2f, 1.2f), floatArrayOf(92f, 300f, 195f, 2f, 1.2f),
        floatArrayOf(330f, 180f, -10f, 3f, 1.2f), floatArrayOf(62f, 170f, 190f, 3f, 1.2f)
    )

    val Leaves: List<TreeLeafDef> = run {
        val perStage = HashMap<Int, Int>()
        LeafData.map { l ->
            val stage = l[3].toInt()
            val order = (perStage[stage] ?: 0) + 1
            perStage[stage] = order
            TreeLeafDef(l[0], l[1], l[2], stage, l[4], order)
        }
    }

    /** Una flor por meta posible, en el mismo orden que las opciones de Objetivo. */
    val FlowerPositions: List<Offset> = listOf(
        Offset(262f, 112f), Offset(128f, 110f), Offset(195f, 82f), Offset(250f, 160f), Offset(140f, 158f)
    )

    /** Hojas que brotan con cada letra del nombre, en abanico sobre la copa. */
    val NameLeaves: List<NameLeafDef> = List(NameLeafMax) { i ->
        val angle = -150f + i * 22f
        val radius = 58f + (i % 2) * 10f
        val rad = Math.toRadians(angle.toDouble())
        NameLeafDef(
            x = (195.0 + cos(rad) * radius).toFloat(),
            y = (200.0 + sin(rad) * radius * 0.8).toFloat(),
            angle = angle
        )
    }

    // Trazos de las formas pequeñas (viewBox de 24 o coordenadas locales)
    const val LeafPath = "M0 0 C6 -8 18 -8 22 0 C18 8 6 8 0 0Z"
    const val NameLeafPath = "M0 0 C5 -6 13 -6 17 0 C13 6 5 6 0 0Z"
    const val FallingLeafPath = "M2 12 C6 4 18 4 22 12 C18 20 6 20 2 12Z"
    const val FallingPetalPath = "M12 3 C17 7 17 15 12 21 C7 15 7 7 12 3Z"

    /** Colina: curva superior de 410 de ancho; el relleno se extiende hacia los lados y abajo. */
    const val HillCurve = "M-10 26 C70 2 140 18 195 14 C255 10 320 -6 400 20"
}
