package com.segundoaire.presentation.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

// Claro es el predeterminado: es lo que se ve fuera de SegundoAireTheme.
val LocalSegundoAireColors = staticCompositionLocalOf { LightSegundoAireColors }
val LocalSegundoAireTypography = staticCompositionLocalOf { DefaultSegundoAireTypography }
val LocalSegundoAireGlass = staticCompositionLocalOf { LightSegundoAireGlass }
val LocalSegundoAireSpacing = staticCompositionLocalOf { SegundoAireSpacing() }
val LocalSegundoAireShapes = staticCompositionLocalOf { SegundoAireShapes() }
val LocalSegundoAireMotion = staticCompositionLocalOf { SegundoAireMotion() }

object SegundoAireTheme {
    val colors: SegundoAireColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSegundoAireColors.current

    val typography: SegundoAireTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalSegundoAireTypography.current

    val glass: SegundoAireGlass
        @Composable
        @ReadOnlyComposable
        get() = LocalSegundoAireGlass.current

    val spacing: SegundoAireSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSegundoAireSpacing.current

    val shapes: SegundoAireShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalSegundoAireShapes.current

    val motion: SegundoAireMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalSegundoAireMotion.current
}

@Composable
fun SegundoAireTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkSegundoAireColors else LightSegundoAireColors
    val reducedMotion = rememberReducedMotion()
    val motion = remember(reducedMotion) { SegundoAireMotion(reducedMotion) }

    CompositionLocalProvider(
        LocalSegundoAireColors provides colors,
        LocalSegundoAireTypography provides DefaultSegundoAireTypography,
        LocalSegundoAireGlass provides if (darkTheme) DarkSegundoAireGlass else LightSegundoAireGlass,
        LocalSegundoAireSpacing provides SegundoAireSpacing(),
        LocalSegundoAireShapes provides SegundoAireShapes(),
        LocalSegundoAireMotion provides motion
    ) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(darkTheme),
            typography = DefaultSegundoAireTypography.toMaterialTypography(),
            content = content
        )
    }
}

// Solo para los componentes de Material 3 que se usen; las pantallas leen SegundoAireTheme.
private fun SegundoAireColors.toColorScheme(darkTheme: Boolean): ColorScheme {
    val base = if (darkTheme) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = secondary,
        onPrimaryContainer = onSecondary,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondary,
        onSecondaryContainer = onSecondary,
        background = background,
        onBackground = onBackground,
        surface = surfaceGlass,
        onSurface = onBackground,
        surfaceVariant = surfaceGlass,
        onSurfaceVariant = onBackgroundMuted,
        surfaceTint = primary,
        error = error,
        onError = errorContainer,
        errorContainer = errorContainer,
        onErrorContainer = error,
        outline = outline,
        outlineVariant = outlineVariant,
        scrim = scrim
    )
}

private fun SegundoAireTypography.toMaterialTypography(): Typography {
    val base = Typography()
    return Typography(
        displayLarge = displayTimer,
        displayMedium = displayImpact,
        displaySmall = base.displaySmall.copy(fontFamily = ManropeFontFamily),
        headlineLarge = headlineLg,
        headlineMedium = headlineMd,
        headlineSmall = base.headlineSmall.copy(fontFamily = ManropeFontFamily),
        titleLarge = titleLg,
        titleMedium = titleMd,
        titleSmall = base.titleSmall.copy(fontFamily = ManropeFontFamily),
        bodyLarge = bodyLg,
        bodyMedium = bodyMd,
        bodySmall = base.bodySmall.copy(fontFamily = ManropeFontFamily),
        labelLarge = labelLg,
        labelMedium = labelMd,
        labelSmall = labelSm
    )
}
