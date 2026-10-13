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

    val Globe: ImageVector by lazy {
        icon("Globe") {
            stroked { circle(centerX = 12f, centerY = 12f, radius = 9f) }
            // Ecuador y meridiano
            stroked {
                moveTo(3f, 12f)
                lineTo(21f, 12f)
            }
            stroked {
                moveTo(12f, 3f)
                curveTo(14.5f, 5.7f, 15.75f, 8.7f, 15.75f, 12f)
                curveTo(15.75f, 15.3f, 14.5f, 18.3f, 12f, 21f)
                curveTo(9.5f, 18.3f, 8.25f, 15.3f, 8.25f, 12f)
                curveTo(8.25f, 8.7f, 9.5f, 5.7f, 12f, 3f)
                close()
            }
        }
    }

    val Device: ImageVector by lazy {
        icon("Device") {
            stroked { roundRect(left = 7f, top = 3f, right = 17f, bottom = 21f, radius = 2.5f) }
            stroked {
                moveTo(11f, 18f)
                lineTo(13f, 18f)
            }
        }
    }

    val Envelope: ImageVector by lazy {
        icon("Envelope") {
            stroked { roundRect(left = 3f, top = 5.5f, right = 21f, bottom = 18.5f, radius = 2.5f) }
            stroked {
                moveTo(4f, 7.5f)
                lineTo(12f, 13f)
                lineTo(20f, 7.5f)
            }
        }
    }

    val Eye: ImageVector by lazy {
        icon("Eye") {
            stroked { eyeOutline() }
            stroked { circle(centerX = 12f, centerY = 12f, radius = 3f) }
        }
    }

    val EyeOff: ImageVector by lazy {
        icon("EyeOff") {
            stroked { eyeOutline() }
            stroked { circle(centerX = 12f, centerY = 12f, radius = 3f) }
            stroked {
                moveTo(4.5f, 4.5f)
                lineTo(19.5f, 19.5f)
            }
        }
    }

    val Error: ImageVector by lazy {
        icon("Error") {
            stroked { circle(centerX = 12f, centerY = 12f, radius = 9f) }
            stroked {
                moveTo(12f, 7.5f)
                lineTo(12f, 12.5f)
            }
            stroked { dot(x = 12f, y = 16f) }
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

private fun ImageVector.Builder.stroked(pathBuilder: PathBuilder.() -> Unit) {
    path(
        stroke = Ink,
        strokeLineWidth = StrokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = pathBuilder
    )
}

private fun PathBuilder.roundRect(left: Float, top: Float, right: Float, bottom: Float, radius: Float) {
    moveTo(left + radius, top)
    lineTo(right - radius, top)
    arcTo(radius, radius, 0f, false, true, right, top + radius)
    lineTo(right, bottom - radius)
    arcTo(radius, radius, 0f, false, true, right - radius, bottom)
    lineTo(left + radius, bottom)
    arcTo(radius, radius, 0f, false, true, left, bottom - radius)
    lineTo(left, top + radius)
    arcTo(radius, radius, 0f, false, true, left + radius, top)
    close()
}

private fun PathBuilder.circle(centerX: Float, centerY: Float, radius: Float) {
    moveTo(centerX + radius, centerY)
    arcToRelative(radius, radius, 0f, true, true, -radius * 2, 0f)
    arcToRelative(radius, radius, 0f, true, true, radius * 2, 0f)
    close()
}

// Un trazo de longitud casi nula con extremos redondeados se dibuja como un punto
private fun PathBuilder.dot(x: Float, y: Float) {
    moveTo(x, y)
    lineTo(x, y + 0.01f)
}

private fun PathBuilder.eyeOutline() {
    moveTo(2.5f, 12f)
    curveTo(5f, 7.5f, 8.5f, 5.5f, 12f, 5.5f)
    curveTo(15.5f, 5.5f, 19f, 7.5f, 21.5f, 12f)
    curveTo(19f, 16.5f, 15.5f, 18.5f, 12f, 18.5f)
    curveTo(8.5f, 18.5f, 5f, 16.5f, 2.5f, 12f)
    close()
}

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
