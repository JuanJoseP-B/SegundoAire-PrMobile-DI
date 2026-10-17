package com.segundoaire.presentation.tree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.vector.PathParser

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
