package com.segundoaire.presentation.designsystem.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Íconos de DESIGN_SYSTEM.md §9: trazo de 1.75 sobre rejilla de 24 dp, con
 * extremos y uniones redondeados. Solo los vectores que la app usa, para no
 * depender de `material-icons-extended`. El color lo pone el `tint` de `Icon`.
 */
object SegundoAireIcons {
    // Play y Stop son los únicos íconos rellenos
    val Play: ImageVector by lazy {
        icon("Play") {
            filled {
                moveTo(8.5f, 6f)
                lineTo(18f, 12f)
                lineTo(8.5f, 18f)
                close()
            }
        }
    }

    val Stop: ImageVector by lazy {
        icon("Stop") {
            filled {
                moveTo(7.5f, 7.5f)
                lineTo(16.5f, 7.5f)
                lineTo(16.5f, 16.5f)
                lineTo(7.5f, 16.5f)
                close()
            }
        }
    }
}

private const val Grid = 24f
private const val StrokeWidth = 1.75f

// El tint de Icon reemplaza este color; solo hace falta que sea opaco
private val Ink = SolidColor(Color.Black)

private fun icon(name: String, paths: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = Grid.dp,
        defaultHeight = Grid.dp,
        viewportWidth = Grid,
        viewportHeight = Grid
    ).apply(paths).build()

// El mismo trazo redondeado sobre el relleno suaviza las esquinas de la figura
private fun ImageVector.Builder.filled(pathBuilder: PathBuilder.() -> Unit) {
    path(
        fill = Ink,
        stroke = Ink,
        strokeLineWidth = StrokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = pathBuilder
    )
}
