package com.segundoaire.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass

/**
 * Acción secundaria: píldora de vidrio nivel 1. El texto va en `onBackground`
 * porque `primary` no cumple contraste sobre vidrio (§3.2); el ícono sí puede
 * usarlo. Hover y pressed se muestran con `stateLayer`.
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null
) {
    val colors = SegundoAireTheme.colors
    val shape = SegundoAireTheme.shapes.radiusFull
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val stateLayer by animateStateLayer(source, enabled)

    ButtonLayout(
        onClick = onClick,
        enabled = enabled,
        interactionSource = source,
        surface = if (enabled) {
            Modifier
                .glass(GlassLevel.Level1, shape)
                .drawBehind { drawRect(stateLayer) }
        } else {
            Modifier
                .clip(shape)
                .background(colors.disabledContainer)
        },
        modifier = modifier
    ) {
        ButtonLabel(
            text = text,
            icon = icon,
            color = if (enabled) colors.onBackground else colors.disabledContent,
            iconTint = if (enabled) colors.primary else colors.disabledContent
        )
    }
}

@Composable
private fun SecondaryButtonSample() {
    val spacing = SegundoAireTheme.spacing
    Column(
        modifier = Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(spacing.space6),
        verticalArrangement = Arrangement.spacedBy(spacing.space4)
    ) {
        SecondaryButton(text = stringResource(R.string.onboarding_continue), onClick = {})
        SecondaryButton(text = stringResource(R.string.onboarding_continue), onClick = {}, enabled = false)
    }
}

@Preview(name = "SecondaryButton · claro")
@Composable
private fun SecondaryButtonLightPreview() {
    SegundoAireTheme(darkTheme = false) { SecondaryButtonSample() }
}

@Preview(name = "SecondaryButton · oscuro")
@Composable
private fun SecondaryButtonDarkPreview() {
    SegundoAireTheme(darkTheme = true) { SecondaryButtonSample() }
}
