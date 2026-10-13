package com.segundoaire.presentation.designsystem.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
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
