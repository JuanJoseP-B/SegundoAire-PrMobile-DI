package com.segundoaire.presentation.onboarding.tree

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.segundoaire.presentation.designsystem.theme.SegundoAireColors
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/** Trazos del árbol ya convertidos a [Path] y medidos, para dibujarlos hasta una fracción. */
internal class TreePaths {
    private fun parse(d: String): Path = PathParser().parsePathString(d).toPath()

    val segments: List<Path> = TreeGeometry.Segments.map { parse(it.d) }
    val roots: List<Path> = TreeGeometry.Roots.map { parse(it.d) }
    val leaf: Path = parse(TreeGeometry.LeafPath)
    val nameLeaf: Path = parse(TreeGeometry.NameLeafPath)
    val hillCurve: Path = parse(TreeGeometry.HillCurve)

    private val segmentLengths: FloatArray
    private val rootLengths: FloatArray
    private val measure = PathMeasure()
    private val scratch = Path()

    init {
        segmentLengths = FloatArray(segments.size) { lengthOf(segments[it]) }
        rootLengths = FloatArray(roots.size) { lengthOf(roots[it]) }
    }

    private fun lengthOf(path: Path): Float {
        measure.setPath(path, false)
        return measure.length
    }

    /** Parte de [path] desde su inicio hasta la fracción [fraction]; el [Path] devuelto se reutiliza. */
    fun partial(path: Path, length: Float, fraction: Float): Path {
        measure.setPath(path, false)
        scratch.rewind()
        measure.getSegment(0f, length * fraction.coerceIn(0f, 1f), scratch, true)
        return scratch
    }

    fun segment(index: Int, fraction: Float): Path = partial(segments[index], segmentLengths[index], fraction)

    fun root(index: Int, fraction: Float): Path = partial(roots[index], rootLengths[index], fraction)
}

@Composable
internal fun rememberTreePaths(): TreePaths = remember { TreePaths() }

private const val Epsilon = 0.002f
private const val FlowerScale = 1.3f
private const val NameLeafScale = 1.2f
private const val WiltedAlpha = 0.45f
private val PetalCenters = arrayOf(
    Offset(0f, -8f), Offset(7.6f, -2.5f), Offset(4.7f, 6.5f), Offset(-4.7f, 6.5f), Offset(-7.6f, -2.5f)
)

/**
 * El árbol en un solo [Canvas], con las coordenadas del SVG del prototipo. Los valores
 * animados se leen aquí, al dibujar, y en `graphicsLayer`: animar no recompone.
 *
 * [rootProgress] da el avance (0–1) de la raíz [Int]; cambia con el botón de compromiso.
 */
@Composable
internal fun OnboardingTree(
    animations: TreeAnimations,
    rootProgress: (index: Int) -> Float,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val paths = rememberTreePaths()

    Canvas(
        modifier = modifier
            .offset(y = TreeGeometry.AreaTop.dp)
            .requiredSize(TreeGeometry.AreaWidth.dp, TreeGeometry.AreaHeight.dp)
            .graphicsLayer {
                translationX = animations.translateX.value * density
                translationY = animations.translateY.value * density
                scaleX = animations.scale.value
                scaleY = animations.scale.value
                transformOrigin = TransformOrigin(0.5f, 0.91f)
                alpha = animations.opacity.value.coerceIn(0f, 1f)
            }
    ) {
        // 1 unidad del SVG = 1 dp del marco; el grupo del prototipo coloca el suelo en (195, 255)
        scale(density, Offset.Zero) {
            translate(TreeGeometry.GroupTranslate.x, TreeGeometry.GroupTranslate.y) {
                scale(TreeGeometry.GroupScale, Offset.Zero) {
                    translate(-TreeGeometry.GroupPivot.x, -TreeGeometry.GroupPivot.y) {
                        drawRoots(animations, paths, colors, rootProgress)
                        drawRipples(animations, colors)
                        drawSeed(animations, colors)
                        rotate(animations.sway.value, TreeGeometry.SwayPivot) {
                            drawCrown(animations, paths, colors)
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawRoots(
    animations: TreeAnimations,
    paths: TreePaths,
    colors: SegundoAireColors,
    rootProgress: (Int) -> Float
) {
    val alpha = animations.rootsAlpha.value
    if (alpha <= Epsilon) return
    TreeGeometry.Roots.forEachIndexed { i, root ->
        val progress = rootProgress(i)
        if (progress > Epsilon) {
            drawPath(
                path = paths.root(i, progress),
                color = colors.secondary.copy(alpha = alpha),
                style = Stroke(width = root.width, cap = StrokeCap.Round)
            )
        }
    }
}

private fun DrawScope.drawRipples(animations: TreeAnimations, colors: SegundoAireColors) {
    val visible = animations.rippleAlpha.value
    if (visible <= Epsilon) return
    animations.ripples.forEach { state ->
        val t = state.value
        if (t > Epsilon) {
            val factor = 0.2f + (2.2f - 0.2f) * t
            val rx = 40f * factor
            val ry = 8f * factor
            drawOval(
                color = colors.primary.copy(alpha = 0.9f * (1f - t) * visible),
                topLeft = Offset(195f - rx, 514f - ry),
                size = Size(rx * 2f, ry * 2f),
                style = Stroke(width = 1.5f * factor)
            )
        }
    }
}

private fun DrawScope.drawSeed(animations: TreeAnimations, colors: SegundoAireColors) {
    val s = animations.seedScale.value
    scale(s, Offset(195f, 510f)) {
        drawOval(colors.primary, Offset(195f - 11f, 510f - 7f), Size(22f, 14f))
    }
}

private fun DrawScope.drawCrown(animations: TreeAnimations, paths: TreePaths, colors: SegundoAireColors) {
    // Tronco y ramas, dibujados como trazos que crecen
    TreeGeometry.Segments.forEachIndexed { i, segment ->
        val progress = animations.segments[i].value
        if (progress > Epsilon) {
            drawPath(
                path = paths.segment(i, progress),
                color = colors.primary,
                style = Stroke(width = segment.width, cap = StrokeCap.Round)
            )
        }
    }

    // Follaje: círculos que escalan desde (50 %, 70 %) y se doran al marchitarse
    TreeGeometry.Blobs.forEachIndexed { i, blob ->
        val s = animations.blobScale[i].value
        if (s > Epsilon) {
            val (base, baseAlpha) = when (blob.colorIndex) {
                0 -> colors.primary to 0.5f
                1 -> colors.glow to 0.55f
                else -> colors.mint to 0.85f
            }
            val wilt = animations.blobWilt[i].value
            val color = lerp(base, colors.gold, wilt)
            val alpha = baseAlpha + (WiltedAlpha - baseAlpha) * wilt
            scale(s, Offset(blob.cx, blob.cy + 0.4f * blob.r)) {
                drawCircle(color.copy(alpha = alpha), blob.r, Offset(blob.cx, blob.cy))
            }
        }
    }

    // Hojas del tronco: escalan desde su base
    TreeGeometry.Leaves.forEachIndexed { i, leaf ->
        val s = animations.leafScale[i].value
        if (s > Epsilon) {
            translate(leaf.x, leaf.y) {
                rotate(leaf.angle, Offset.Zero) {
                    scale(leaf.scale * s, Offset.Zero) {
                        drawPath(paths.leaf, colors.glow.copy(alpha = 0.85f))
                    }
                }
            }
        }
    }

    // Una hoja por letra del nombre
    TreeGeometry.NameLeaves.forEachIndexed { i, leaf ->
        val s = animations.nameLeafScale[i].value
        if (s > Epsilon) {
            translate(leaf.x, leaf.y) {
                rotate(leaf.angle, Offset.Zero) {
                    scale(NameLeafScale * s, Offset.Zero) {
                        drawPath(paths.nameLeaf, colors.secondary.copy(alpha = 0.9f))
                    }
                }
            }
        }
    }

    // Una flor por meta: gira y crece al abrirse
    TreeGeometry.FlowerPositions.forEachIndexed { i, position ->
        val p = animations.flowerProgress[i].value
        if (p > Epsilon) {
            translate(position.x, position.y) {
                scale(FlowerScale * p, Offset.Zero) {
                    rotate(-90f * (1f - p), Offset.Zero) {
                        drawFlower(colors.blossom, colors.gold)
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
