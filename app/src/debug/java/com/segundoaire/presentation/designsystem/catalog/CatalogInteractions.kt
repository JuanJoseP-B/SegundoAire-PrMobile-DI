package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Fuente congelada en "presionado", para mostrar ese estado sin tocar la pantalla. */
@Composable
fun rememberPressedInteractionSource(): MutableInteractionSource =
    remember { FixedInteractionSource(PressInteraction.Press(Offset.Zero)) }

/** Fuente congelada en "con foco": en modo táctil el foco no se ve de otra forma. */
@Composable
fun rememberFocusedInteractionSource(): MutableInteractionSource =
    remember { FixedInteractionSource(FocusInteraction.Focus()) }

// Ignora lo que emita el componente: su estado no cambia al tocarlo
private class FixedInteractionSource(interaction: Interaction) : MutableInteractionSource {
    override val interactions: Flow<Interaction> = flowOf(interaction)

    override suspend fun emit(interaction: Interaction) = Unit

    override fun tryEmit(interaction: Interaction): Boolean = true
}
