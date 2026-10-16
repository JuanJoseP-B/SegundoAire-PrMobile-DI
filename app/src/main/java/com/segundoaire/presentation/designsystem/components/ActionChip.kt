package com.segundoaire.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass

// Chip de la referencia: 40 dp de alto visible dentro de 48 dp de área táctil
private val ChipHeight = 40.dp
private val ChipTouchHeight = 48.dp
private val ChipIconSize = 16.dp

/** Texto de los chips (13 / 18 sp, peso 600). */
val chipTextStyle: TextStyle
    @Composable get() = SegundoAireTheme.typography.labelLg.copy(fontSize = 13.sp, lineHeight = 18.sp)

/**
 * Chip de acción de la app principal: píldora de vidrio con borde de luz o, con
 * [solid], relleno `primary`. 40 dp visibles dentro de un área táctil de 48 dp,
 * con squish y anillo de foco. Sin [onClick] es solo una etiqueta (las metas del Perfil).
 */
@Composable
fun ActionChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    solid: Boolean = false,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    val colors = SegundoAireTheme.colors
    val shape = CircleShape
    val source = remember { MutableInteractionSource() }
    val content = if (solid) colors.onPrimary else colors.onBackground
    val surface = if (solid) Modifier.background(colors.primary, shape) else Modifier.glass(GlassLevel.Level2, shape)
    val clickable = if (onClick != null) {
        Modifier
            .pressScale(source, enabled)
            .clickable(
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .heightIn(min = if (onClick != null) ChipTouchHeight else ChipHeight)
            .then(clickable),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .focusRing(source, shape)
                .heightIn(min = ChipHeight)
                .then(surface)
                .padding(horizontal = SegundoAireTheme.spacing.space3 + 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = content, modifier = Modifier.size(ChipIconSize))
                Spacer(Modifier.width(6.dp))
            }
            Text(text = text, style = chipTextStyle, color = content)
        }
    }
}

@Composable
private fun ActionChipSample() {
    Row(
        Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(SegundoAireTheme.spacing.space4),
        horizontalArrangement = Arrangement.spacedBy(SegundoAireTheme.spacing.space2)
    ) {
        ActionChip(text = stringResource(R.string.nav_blocks), onClick = {}, solid = true)
        ActionChip(text = stringResource(R.string.nav_progress), onClick = {})
        ActionChip(text = stringResource(R.string.nav_panel), leadingIcon = SegundoAireIcons.Panel)
    }
}

@Preview(name = "ActionChip · claro")
@Composable
private fun ActionChipLightPreview() {
    SegundoAireTheme(darkTheme = false) { ActionChipSample() }
}

@Preview(name = "ActionChip · oscuro")
@Composable
private fun ActionChipDarkPreview() {
    SegundoAireTheme(darkTheme = true) { ActionChipSample() }
}
