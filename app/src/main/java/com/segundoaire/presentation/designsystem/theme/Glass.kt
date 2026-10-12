package com.segundoaire.presentation.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class GlassLevel { Level1, Level2, Level3 }

@Immutable
data class GlassShadow(
    val offsetY: Dp,
    val blur: Dp,
    val color: Color
)

@Immutable
data class GlassStyle(
    val fill: Color,
    // Relleno en Android 10–11, sin blur: 20 puntos más de opacidad
    val fillNoBlur: Color,
    val blur: Dp,
    val shadows: List<GlassShadow>
)

// Niveles de DESIGN_SYSTEM.md §5. El borde es común a los tres.
@Immutable
data class SegundoAireGlass(
    val level1: GlassStyle,
    val level2: GlassStyle,
    val level3: GlassStyle,
    val edgeHi: Color,
    val edgeLo: Color,
    val edgeWidth: Dp = 1.dp
) {
    operator fun get(level: GlassLevel): GlassStyle = when (level) {
        GlassLevel.Level1 -> level1
        GlassLevel.Level2 -> level2
        GlassLevel.Level3 -> level3
    }
}

private const val NoBlurOpacityBoost = 0.20f

private fun glassStyle(fill: Color, blur: Dp, vararg shadows: GlassShadow) = GlassStyle(
    fill = fill,
    fillNoBlur = fill.copy(alpha = fill.alpha + NoBlurOpacityBoost),
    blur = blur,
    shadows = shadows.toList()
)

private fun shadow(offsetY: Int, blur: Int, base: Color, opacity: Float) =
    GlassShadow(offsetY = offsetY.dp, blur = blur.dp, color = base.copy(alpha = opacity))

// La sombra en claro se tiñe con el mismo tono que onBackground (#051F20)
private val LightShadow = LightSegundoAireColors.onBackground
private val DarkShadow = Color.Black

val LightSegundoAireGlass = SegundoAireGlass(
    level1 = glassStyle(
        LightSegundoAireColors.glass1,
        12.dp,
        shadow(1, 2, LightShadow, 0.06f)
    ),
    level2 = glassStyle(
        LightSegundoAireColors.glass2,
        20.dp,
        shadow(2, 6, LightShadow, 0.06f),
        shadow(8, 24, LightShadow, 0.08f)
    ),
    level3 = glassStyle(
        LightSegundoAireColors.glass3,
        32.dp,
        shadow(4, 12, LightShadow, 0.08f),
        shadow(16, 48, LightShadow, 0.14f)
    ),
    edgeHi = LightSegundoAireColors.glassEdgeHi,
    edgeLo = LightSegundoAireColors.glassEdgeLo
)

val DarkSegundoAireGlass = SegundoAireGlass(
    level1 = glassStyle(
        DarkSegundoAireColors.glass1,
        12.dp,
        shadow(1, 2, DarkShadow, 0.20f)
    ),
    level2 = glassStyle(
        DarkSegundoAireColors.glass2,
        20.dp,
        shadow(2, 6, DarkShadow, 0.22f),
        shadow(8, 24, DarkShadow, 0.28f)
    ),
    level3 = glassStyle(
        DarkSegundoAireColors.glass3,
        32.dp,
        shadow(4, 12, DarkShadow, 0.28f),
        shadow(16, 48, DarkShadow, 0.40f)
    ),
    edgeHi = DarkSegundoAireColors.glassEdgeHi,
    edgeLo = DarkSegundoAireColors.glassEdgeLo
)
