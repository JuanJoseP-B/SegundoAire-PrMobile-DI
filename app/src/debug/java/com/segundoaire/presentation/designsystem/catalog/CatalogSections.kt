package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.BlockButton
import com.segundoaire.presentation.designsystem.components.PrimaryButton
import com.segundoaire.presentation.designsystem.components.SecondaryButton
import com.segundoaire.presentation.designsystem.components.pressScale
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/** Una sección por componente, en el orden de la Fase 10 del ROADMAP. */
@Composable
fun CatalogSections() {
    PressScaleSection()
    PrimaryButtonSection()
    SecondaryButtonSection()
}

@Composable
private fun PressScaleSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_press_scale)) {
        val colors = SegundoAireTheme.colors
        val spacing = SegundoAireTheme.spacing
        val interactionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pressScale(interactionSource)
                .clip(SegundoAireTheme.shapes.radiusLg)
                .background(colors.primary)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = {}
                )
                .padding(spacing.space6),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.catalog_press_and_hold),
                style = SegundoAireTheme.typography.labelLg,
                color = colors.onPrimary
            )
        }
    }
}

@Composable
private fun PrimaryButtonSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_primary_button)) {
        // Morph Iniciar ↔ Terminar: se alterna al tocarlo
        var blockActive by rememberSaveable { mutableStateOf(false) }
        BlockButton(
            active = blockActive,
            onClick = { blockActive = !blockActive },
            modifier = Modifier.fillMaxWidth()
        )
        ButtonStates { text, enabled, interactionSource ->
            PrimaryButton(
                text = text,
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                interactionSource = interactionSource
            )
        }
    }
}

@Composable
private fun SecondaryButtonSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_secondary_button)) {
        ButtonStates { text, enabled, interactionSource ->
            SecondaryButton(
                text = text,
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                interactionSource = interactionSource
            )
        }
    }
}

/** Los cuatro estados de un botón; pressed y foco quedan fijos para poder compararlos. */
@Composable
private fun ButtonStates(
    button: @Composable (text: String, enabled: Boolean, interactionSource: MutableInteractionSource?) -> Unit
) {
    button(stringResource(R.string.catalog_state_default), true, null)
    button(stringResource(R.string.catalog_state_pressed), true, rememberPressedInteractionSource())
    button(stringResource(R.string.catalog_state_focused), true, rememberFocusedInteractionSource())
    button(stringResource(R.string.catalog_state_disabled), false, null)
}
