package com.segundoaire.presentation.designsystem.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

private val AppIconSize = 48.dp

/**
 * Muestra el marcador geométrico de inmediato y, cuando el ícono real llega
 * (fuera del hilo principal), lo funde encima. Si no hay ícono, queda el marcador.
 */
@Composable
fun AppIcon(
    packageName: String,
    loadIcon: suspend (packageName: String) -> ImageBitmap?,
    modifier: Modifier = Modifier
) {
    val motion = SegundoAireTheme.motion
    var icon by remember(packageName) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(packageName) {
        icon = loadIcon(packageName)
    }
    Crossfade(
        targetState = icon,
        modifier = modifier,
        animationSpec = motion.calm(),
        label = "appIcon"
    ) { loaded ->
        if (loaded != null) {
            Image(
                bitmap = loaded,
                contentDescription = null,
                modifier = Modifier
                    .size(AppIconSize)
                    .clip(SegundoAireTheme.shapes.radiusSm)
            )
        } else {
            AppIconPlaceholder(index = packageName.hashCode())
        }
    }
}
