package com.segundoaire.presentation.profile

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.FileProvider
import com.segundoaire.domain.model.DayMinutes
import com.segundoaire.domain.model.TreeState
import com.segundoaire.presentation.designsystem.theme.SegundoAireColors
import com.segundoaire.presentation.onboarding.toPickIndexes
import com.segundoaire.presentation.panel.HillPath
import com.segundoaire.presentation.progress.TrunkCutWidth
import com.segundoaire.presentation.progress.drawTrunkCut
import com.segundoaire.presentation.tree.TreeGeometry
import com.segundoaire.presentation.tree.TreePaths
import com.segundoaire.presentation.tree.drawGrownTree
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * "Compartir mi árbol": dibuja el árbol (flores y hojas de la semana) sobre su colina y,
 * debajo, el corte del tronco con los anillos, y lo entrega con un `ACTION_SEND`. La
 * imagen no lleva nombre, correo ni ningún otro dato personal.
 */
internal object ShareTree {
    // Imagen de 1080 × 1500 sobre un marco de 390 × 541: el árbol arriba y el corte debajo
    private const val WidthPx = 1080
    private const val HeightPx = 1500
    private const val FrameWidth = 390f
    private const val TreeScale = 0.5f
    private const val TreeBaseY = 270f
    private const val HillShift = TreeBaseY - 256f
    private const val RingsTop = 300f
    private const val FileName = "tree.png"
    private const val Folder = "shared"

    /** Dibuja y guarda la imagen fuera del hilo principal; `null` si no se pudo escribir. */
    suspend fun render(context: Context, tree: TreeState, rings: List<DayMinutes>, colors: SegundoAireColors): File? =
        withContext(Dispatchers.Default) {
            val bitmap = ImageBitmap(WidthPx, HeightPx)
            val unit = WidthPx / FrameWidth
            val paths = TreePaths()
            val hill = PathParser().parsePathString(HillPath).toPath()
            val flowers = tree.goals.toPickIndexes()
            CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(bitmap), Size(WidthPx.toFloat(), HeightPx.toFloat())) {
                drawRect(colors.background)
                scale(unit, Offset.Zero) {
                    translate(top = HillShift) { drawPath(hill, colors.hill) }
                    translate(TreeGeometry.SwayPivot.x, TreeBaseY) {
                        scale(TreeScale, Offset.Zero) {
                            translate(-TreeGeometry.SwayPivot.x, -TreeGeometry.SwayPivot.y) {
                                drawGrownTree(paths, colors, flowers, tree.leafCount)
                            }
                        }
                    }
                    translate((FrameWidth - TrunkCutWidth) / 2f, RingsTop) {
                        drawTrunkCut(rings, colors)
                    }
                }
            }
            withContext(Dispatchers.IO) {
                try {
                    val folder = File(context.cacheDir, Folder).apply { mkdirs() }
                    File(folder, FileName).also { file ->
                        file.outputStream().use { bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
                    }
                } catch (_: IOException) {
                    null
                }
            }
        }

    /** Abre el selector del sistema con la imagen y una frase corta. */
    fun share(context: Context, file: File, chooserTitle: String, text: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, text)
            clipData = ClipData.newRawUri(null, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, chooserTitle))
    }
}
