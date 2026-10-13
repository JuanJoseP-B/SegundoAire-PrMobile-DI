package com.segundoaire.presentation.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

// Radios de DESIGN_SYSTEM.md §6.
@Immutable
data class SegundoAireShapes(
    val radiusXs: CornerBasedShape = RoundedCornerShape(8.dp),
    val radiusSm: CornerBasedShape = RoundedCornerShape(12.dp),
    val radiusMd: CornerBasedShape = RoundedCornerShape(16.dp),
    val radiusLg: CornerBasedShape = RoundedCornerShape(24.dp),
    val radiusXl: CornerBasedShape = RoundedCornerShape(32.dp),
    val radiusFull: CornerBasedShape = CircleShape
)
