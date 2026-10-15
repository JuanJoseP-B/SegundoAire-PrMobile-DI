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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/**
 * Acción de menor peso, sin superficie. El [color] por defecto es `primary`,
 * que como texto solo cumple contraste sobre `background`: encima de vidrio
 * hay que pasar `onBackground` (§3.2).
 */
@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = SegundoAireTheme.colors.primary,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val stateLayer by animateStateLayer(source, enabled)

    ButtonLayout(
        onClick = onClick,
        enabled = enabled,
        interactionSource = source,
        surface = Modifier
            .clip(SegundoAireTheme.shapes.radiusFull)
            .drawBehind { drawRect(stateLayer) },
        modifier = modifier
    ) {
        ButtonLabel(
            text = text,
            icon = null,
            color = if (enabled) color else SegundoAireTheme.colors.disabledContent
        )
    }
}

@Composable
private fun TextButtonSample() {
    val spacing = SegundoAireTheme.spacing
    Column(
        modifier = Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(spacing.space6),
        verticalArrangement = Arrangement.spacedBy(spacing.space4)
    ) {
        TextButton(text = stringResource(R.string.permission_skip), onClick = {})
        TextButton(text = stringResource(R.string.permission_skip), onClick = {}, enabled = false)
    }
}

@Preview(name = "TextButton · claro")
@Composable
private fun TextButtonLightPreview() {
    SegundoAireTheme(darkTheme = false) { TextButtonSample() }
}

@Preview(name = "TextButton · oscuro")
@Composable
private fun TextButtonDarkPreview() {
    SegundoAireTheme(darkTheme = true) { TextButtonSample() }
}
