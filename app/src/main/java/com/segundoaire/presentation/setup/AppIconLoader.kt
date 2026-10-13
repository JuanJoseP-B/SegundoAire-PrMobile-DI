package com.segundoaire.presentation.setup

import android.content.Context
import android.content.pm.PackageManager
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Carga el ícono de una app cuando su fila entra en pantalla. Decodificarlo es
 * trabajo de disco y de dibujo, por eso `load` nunca corre en el hilo principal.
 */
fun interface AppIconLoader {
    /** `null` si la app ya no está instalada o no tiene ícono legible. */
    suspend fun load(packageName: String): ImageBitmap?
}

@Singleton
class PackageManagerAppIconLoader @Inject constructor(
    @ApplicationContext context: Context
) : AppIconLoader {

    private val packageManager: PackageManager = context.packageManager
    private val sizePx = (ICON_SIZE_DP * context.resources.displayMetrics.density).roundToInt()

    // Acotada en bytes: cientos de apps no deben llenar la memoria al desplazarse
    private val cache = object : LruCache<String, ImageBitmap>(CACHE_BYTES) {
        override fun sizeOf(key: String, value: ImageBitmap): Int = value.width * value.height * BYTES_PER_PIXEL
    }

    override suspend fun load(packageName: String): ImageBitmap? {
        cache.get(packageName)?.let { return it }
        return withContext(Dispatchers.IO) {
            try {
                packageManager.getApplicationIcon(packageName)
                    .toBitmap(sizePx, sizePx)
                    .asImageBitmap()
                    .also { cache.put(packageName, it) }
            } catch (_: PackageManager.NameNotFoundException) {
                null
            }
        }
    }

    private companion object {
        const val ICON_SIZE_DP = 48
        const val BYTES_PER_PIXEL = 4
        const val CACHE_BYTES = 4 * 1024 * 1024
    }
}
