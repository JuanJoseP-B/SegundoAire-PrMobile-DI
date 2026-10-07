package com.segundoaire.presentation.theme

import androidx.compose.ui.graphics.Color

// Light (predeterminado)
val LightPrimary = Color(0xFF2F7F86)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightBackground = Color(0xFFF2F7F8)
val LightOnBackground = Color(0xFF14272A)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF14272A)
val LightOnSurfaceVariant = Color(0xFF4A6366)

// Dark (contraste alto sobre fondo profundo, sin negro puro)
val DarkPrimary = Color(0xFF7FD3DB)
val DarkOnPrimary = Color(0xFF00363B)
val DarkBackground = Color(0xFF0E1A1C)
val DarkOnBackground = Color(0xFFE4F1F2)
val DarkSurface = Color(0xFF162427)
val DarkOnSurface = Color(0xFFE4F1F2)
val DarkOnSurfaceVariant = Color(0xFFA9C4C7)

// Glassmorphism: superficies translúcidas y borde sutil
val LightGlassSurface = Color(0xFFFFFFFF).copy(alpha = 0.55f)
val LightGlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.80f)
val DarkGlassSurface = Color(0xFF1F3438).copy(alpha = 0.55f)
val DarkGlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.14f)
