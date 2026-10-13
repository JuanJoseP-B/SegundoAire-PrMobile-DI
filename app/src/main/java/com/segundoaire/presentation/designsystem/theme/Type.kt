package com.segundoaire.presentation.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

private const val TabularNums = "tnum"

// Escala de DESIGN_SYSTEM.md §4.
@Immutable
data class SegundoAireTypography(
    val displayTimer: TextStyle,
    val displayImpact: TextStyle,
    val headlineLg: TextStyle,
    val headlineMd: TextStyle,
    val titleLg: TextStyle,
    val titleMd: TextStyle,
    val bodyLg: TextStyle,
    val bodyMd: TextStyle,
    val labelLg: TextStyle,
    val labelMd: TextStyle,
    val labelSm: TextStyle
) {
    // labelSm en mayúsculas completas (eyebrows) abre el tracking a 0.08 em
    val labelSmCaps: TextStyle
        get() = labelSm.copy(letterSpacing = 0.08.em)
}

val DefaultSegundoAireTypography = SegundoAireTypography(
    displayTimer = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.W300,
        fontSize = 72.sp,
        lineHeight = 76.sp,
        letterSpacing = (-0.02).em,
        fontFeatureSettings = TabularNums
    ),
    displayImpact = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.W700,
        fontSize = 44.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.02).em,
        fontFeatureSettings = TabularNums
    ),
    headlineLg = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.01).em
    ),
    headlineMd = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.01).em
    ),
    titleLg = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.em
    ),
    titleMd = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.005.em
    ),
    bodyLg = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.em
    ),
    bodyMd = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.em
    ),
    labelLg = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.01.em
    ),
    labelMd = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.02.em
    ),
    labelSm = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.06.em
    )
)
