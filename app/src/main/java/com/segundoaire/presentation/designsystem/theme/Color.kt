package com.segundoaire.presentation.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Tokens de DESIGN_SYSTEM.md §3.1. Único archivo donde vive un hex.
@Immutable
data class SegundoAireColors(
    val background: Color,
    val onBackground: Color,
    val onBackgroundMuted: Color,
    val surfaceGlass: Color,
    val glass1: Color,
    val glass2: Color,
    val glass3: Color,
    // Centro del blob del splash
    val mint: Color,
    val glassEdgeHi: Color,
    val glassEdgeLo: Color,
    val primary: Color,
    val primaryHover: Color,
    val primaryPressed: Color,
    val onPrimary: Color,
    // Solo decorativo (2.77:1), nunca texto ni borde
    val primaryAlt: Color,
    val secondary: Color,
    val secondaryDeep: Color,
    val onSecondary: Color,
    val outline: Color,
    val outlineVariant: Color,
    val glow: Color,
    val onGlow: Color,
    val focusRing: Color,
    val stateLayer: Color,
    val disabledContent: Color,
    val disabledContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val scrim: Color,
    val app1: Color,
    val app2: Color,
    val app3: Color,
    val app4: Color,
    val app5: Color,
    // Árbol del onboarding: follaje marchito, pétalos y luciérnagas
    val gold: Color,
    val blossom: Color,
    val firefly: Color,
    // Luz del suelo según el momento del día (ya con su opacidad)
    val skyMorning: Color,
    val skyAfternoon: Color,
    val skyNight: Color,
    val skyLate: Color,
    // App principal (§3.4): árbol del Panel, corte del tronco y tarjeta del coach
    val hill: Color,
    val trunk: Color,
    val leaf: Color,
    val petal: Color,
    val wood: Color,
    val aiCard: Color,
    // Tonos del follaje, ya con su opacidad; se alternan por círculo
    val tree1: Color,
    val tree2: Color,
    val tree3: Color,
    // Luz radial del héroe del Panel según la hora real (ya con su opacidad)
    val heroSkyMorning: Color,
    val heroSkyDay: Color,
    val heroSkyEvening: Color,
    val heroSkyNight: Color,
    // GlassWall a pantalla completa
    val wallScrim: Color,
    val wallOn: Color,
    val wallMuted: Color,
    val wallTrunk: Color,
    val wallLeaf: Color,
    val wallBtn: Color,
    val wallBtnOn: Color,
    val wallChip: Color,
    val wallChipEdge: Color,
    // Copa gris (invierno) del GlassWall en Strict
    val wallWinter: Color
)

// Claro (predeterminado)
val LightSegundoAireColors = SegundoAireColors(
    background = Color(0xFFEAF4EC),
    onBackground = Color(0xFF051F20),
    onBackgroundMuted = Color(0xFF3D5A50),
    surfaceGlass = Color(0xFFF4FAF6),
    glass1 = Color(0x61FFFFFF),
    glass2 = Color(0x94FFFFFF),
    glass3 = Color(0xBDFFFFFF),
    mint = Color(0xFF8EB69B),
    glassEdgeHi = Color(0xB3FFFFFF),
    glassEdgeLo = Color(0x14FFFFFF),
    primary = Color(0xFF235347),
    primaryHover = Color(0xFF2A5E51),
    primaryPressed = Color(0xFF1B443A),
    onPrimary = Color(0xFFDAF1DE),
    primaryAlt = Color(0xFF235347),
    secondary = Color(0xFF163832),
    secondaryDeep = Color(0xFF0B2B26),
    onSecondary = Color(0xFFDAF1DE),
    outline = Color(0xFF4A7A63),
    outlineVariant = Color(0x1F051F20),
    glow = Color(0xFF2E7D63),
    onGlow = Color(0xFFDAF1DE),
    focusRing = Color(0xFF235347),
    stateLayer = Color(0x14051F20),
    disabledContent = Color(0x61051F20),
    disabledContainer = Color(0x1A051F20),
    error = Color(0xFF9B2C22),
    errorContainer = Color(0xFFF3D6D0),
    success = Color(0xFF1F6B3A),
    successContainer = Color(0xFFCDEBD3),
    warning = Color(0xFF7A4F00),
    warningContainer = Color(0xFFF2E2BE),
    scrim = Color(0x52051F20),
    app1 = Color(0xFFC98B6B),
    app2 = Color(0xFF6B8FC9),
    app3 = Color(0xFFB58BC9),
    app4 = Color(0xFFC9B26B),
    app5 = Color(0xFF6BC9B5),
    gold = Color(0xFFC9B26B),
    blossom = Color(0xFFDAF1DE),
    firefly = Color(0xFFE6F5B0),
    skyMorning = Color(0x73FFC496),
    skyAfternoon = Color(0x73FFF6BE),
    skyNight = Color(0xBF0E1640),
    skyLate = Color(0x9E0D1F23),
    hill = Color(0x808EB69B),
    trunk = Color(0xFF235347),
    leaf = Color(0xFF2E7D63),
    petal = Color(0xFFF4FBF5),
    wood = Color(0x598EB69B),
    aiCard = Color(0xEBF0FAF2),
    tree1 = Color(0x80235347),
    tree2 = Color(0x8C2E7D63),
    tree3 = Color(0xD98EB69B),
    heroSkyMorning = Color(0x66FFC896),
    heroSkyDay = Color(0x73FFF6BE),
    heroSkyEvening = Color(0x6BF4A06E),
    heroSkyNight = Color(0x380D1F23),
    wallScrim = Color(0xC7DAF1DE),
    wallOn = Color(0xFF051F20),
    wallMuted = Color(0xFF2C5649),
    wallTrunk = Color(0xFF235347),
    wallLeaf = Color(0xFF2E7D63),
    wallBtn = Color(0xFF235347),
    wallBtnOn = Color(0xFFDAF1DE),
    wallChip = Color(0x14051F20),
    wallChipEdge = Color(0xB3FFFFFF),
    wallWinter = Color(0x999AA8A2)
)

val DarkSegundoAireColors = SegundoAireColors(
    background = Color(0xFF0D1F23),
    onBackground = Color(0xFFB0B4B8),
    onBackgroundMuted = Color(0xFF8C9297),
    surfaceGlass = Color(0xFF132E35),
    glass1 = Color(0x73132E35),
    glass2 = Color(0xA6132E35),
    glass3 = Color(0xD1132E35),
    mint = Color(0xFF86E0CF),
    glassEdgeHi = Color(0x47B0B4B8),
    glassEdgeLo = Color(0x0AB0B4B8),
    primary = Color(0xFF708792),
    primaryHover = Color(0xFF7A909B),
    primaryPressed = Color(0xFF8699A3),
    onPrimary = Color(0xFF0D1F23),
    primaryAlt = Color(0xFF5A636A),
    secondary = Color(0xFF2D4A53),
    secondaryDeep = Color(0xFF091619),
    onSecondary = Color(0xFFB0B4B8),
    outline = Color(0xFF5E7A84),
    outlineVariant = Color(0x1FB0B4B8),
    glow = Color(0xFF86E0CF),
    onGlow = Color(0xFF0D1F23),
    focusRing = Color(0xFF86E0CF),
    stateLayer = Color(0x1AB0B4B8),
    disabledContent = Color(0x61B0B4B8),
    disabledContainer = Color(0x1AB0B4B8),
    error = Color(0xFFF2A497),
    errorContainer = Color(0xFF3A1D1A),
    success = Color(0xFFA6D99A),
    successContainer = Color(0xFF1C3320),
    warning = Color(0xFFE6C47E),
    warningContainer = Color(0xFF33290F),
    scrim = Color(0x99040C0E),
    app1 = Color(0xFFB07A5F),
    app2 = Color(0xFF5F7DB0),
    app3 = Color(0xFF9C7AB0),
    app4 = Color(0xFFB09B5F),
    app5 = Color(0xFF5FB09E),
    gold = Color(0xFFB09B5F),
    blossom = Color(0xFFD0E8D6),
    firefly = Color(0xFFE6F5B0),
    skyMorning = Color(0x73FFC496),
    skyAfternoon = Color(0x73FFF6BE),
    skyNight = Color(0xBF050A24),
    skyLate = Color(0x9E040C0E),
    hill = Color(0xF2132E35),
    trunk = Color(0xFF5E7A84),
    leaf = Color(0xFF86E0CF),
    petal = Color(0xFFDCE3E0),
    wood = Color(0xE6132E35),
    aiCard = Color(0xFF11282D),
    tree1 = Color(0xE62D4A53),
    tree2 = Color(0x993E8C7A),
    tree3 = Color(0x5986E0CF),
    heroSkyMorning = Color(0x1FFFC896),
    heroSkyDay = Color(0x14FFF6BE),
    heroSkyEvening = Color(0x24F4A06E),
    heroSkyNight = Color(0x000D1F23),
    wallScrim = Color(0xB8040C0E),
    wallOn = Color(0xFFDCE3E0),
    wallMuted = Color(0xFF8C9297),
    wallTrunk = Color(0xFF5E7A84),
    wallLeaf = Color(0xFF86E0CF),
    wallBtn = Color(0xFF708792),
    wallBtnOn = Color(0xFF0D1F23),
    wallChip = Color(0x24B0B4B8),
    wallChipEdge = Color(0x2EB0B4B8),
    wallWinter = Color(0x993A4A50)
)

/**
 * Modo enfoque (§3.4): siempre oscuro, igual en claro y en oscuro. El fondo es el
 * `secondaryDeep` claro (`#0B2B26`); el anillo, la punta y el botón repiten los tonos
 * oscuros de la app para que la pantalla sea la misma en los dos temas.
 */
object FocusColors {
    val Background = Color(0xFF0B2B26)
    val On = Color(0xFFDAF1DE)
    val Muted = Color(0xB3DAF1DE)
    val Quote = Color(0xD9DAF1DE)
    val Track = Color(0x1FDAF1DE)
    val Progress = Color(0xFF708792)
    val Tip = Color(0xFF86E0CF)
    val Button = Color(0xFF2D4A53)
    val TreeLight = Color(0xFF8EB69B)
    val TreeDeep = Color(0xFF2E7D63)
}

/**
 * Colores de la marca Google para su logotipo en el botón de proveedor. No cambian con el
 * tema: las guías de la marca exigen sus cuatro colores en claro y en oscuro.
 */
object GoogleLogoColors {
    val Red = Color(0xFFEA4335)
    val Blue = Color(0xFF4285F4)
    val Yellow = Color(0xFFFBBC05)
    val Green = Color(0xFF34A853)
}
