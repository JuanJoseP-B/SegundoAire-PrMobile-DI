package com.segundoaire.presentation.designsystem.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.segundoaire.R

// Un solo archivo variable; cada peso fija el eje wght.
@OptIn(ExperimentalTextApi::class)
private fun manrope(weight: FontWeight) = Font(
    resId = R.font.manrope_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

val ManropeFontFamily = FontFamily(
    manrope(FontWeight.W300),
    manrope(FontWeight.W400),
    manrope(FontWeight.W500),
    manrope(FontWeight.W600),
    manrope(FontWeight.W700)
)

// Fraunces: serif de los títulos del onboarding. Dos archivos variables (recto y cursiva);
// cada peso fija wght y el eje opsz queda en 96 (el contraste alto de los títulos grandes).
private const val FrauncesOpticalSize = 96f

@OptIn(ExperimentalTextApi::class)
private fun fraunces(weight: FontWeight, italic: Boolean) = Font(
    resId = if (italic) R.font.fraunces_italic_variable else R.font.fraunces_variable,
    weight = weight,
    style = if (italic) FontStyle.Italic else FontStyle.Normal,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight.weight),
        FontVariation.Setting("opsz", FrauncesOpticalSize)
    )
)

val FrauncesFontFamily = FontFamily(
    fraunces(FontWeight.W400, italic = false),
    fraunces(FontWeight.W500, italic = false),
    fraunces(FontWeight.W400, italic = true),
    fraunces(FontWeight.W500, italic = true)
)
