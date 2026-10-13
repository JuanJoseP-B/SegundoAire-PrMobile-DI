package com.segundoaire.presentation.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass

private val FieldBorderWidth = 1.dp
private val FieldBorderWidthActive = 2.dp
private val FieldIconSize = 20.dp
private val FieldMessageIconSize = 16.dp

/**
 * Campo de texto de una línea sobre vidrio nivel 1. El borde pasa de `outline`
 * a `focusRing` con el foco y a `error` si hay [errorMessage], que se muestra
 * con ícono y texto (§3.2). Con [isPassword] oculta el contenido y ofrece el
 * botón de mostrar/ocultar.
 */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    errorMessage: String? = null,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = if (isPassword) {
        KeyboardOptions(keyboardType = KeyboardType.Password)
    } else {
        KeyboardOptions.Default
    },
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource? = null
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val typography = SegundoAireTheme.typography
    val shape = SegundoAireTheme.shapes.radiusMd
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val isError = errorMessage != null
    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.disabledContent
            isError -> colors.error
            focused -> colors.focusRing
            else -> colors.outline
        },
        animationSpec = SegundoAireTheme.motion.press(),
        label = "fieldBorder"
    )
    val contentColor = if (enabled) colors.onBackground else colors.disabledContent

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.space2)
    ) {
        Text(text = label, style = typography.labelMd, color = contentColor)

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = label
                    if (errorMessage != null) error(errorMessage)
                },
            enabled = enabled,
            textStyle = typography.bodyLg.copy(color = contentColor),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            interactionSource = source,
            cursorBrush = SolidColor(colors.primary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .glass(GlassLevel.Level1, shape)
                        .border(
                            width = if (focused || isError) FieldBorderWidthActive else FieldBorderWidth,
                            color = borderColor,
                            shape = shape
                        )
                        .defaultMinSize(minHeight = ButtonMinSize)
                        .padding(start = spacing.space4, end = if (isPassword) 0.dp else spacing.space4),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = spacing.space3)
                    ) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = typography.bodyLg,
                                color = if (enabled) colors.onBackgroundMuted else colors.disabledContent,
                                maxLines = 1
                            )
                        }
                        innerTextField()
                    }
                    if (isPassword) {
                        PasswordVisibilityButton(
                            visible = passwordVisible,
                            enabled = enabled,
                            onToggle = { passwordVisible = !passwordVisible }
                        )
                    }
                }
            }
        )

        if (errorMessage != null) {
            Row(
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                horizontalArrangement = Arrangement.spacedBy(spacing.space1),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = SegundoAireIcons.Error,
                    contentDescription = null,
                    tint = colors.error,
                    modifier = Modifier.size(FieldMessageIconSize)
                )
                Text(text = errorMessage, style = typography.bodyMd, color = colors.error)
            }
        } else if (supportingText != null) {
            Text(
                text = supportingText,
                style = typography.bodyMd,
                color = if (enabled) colors.onBackgroundMuted else colors.disabledContent
            )
        }
    }
}

@Composable
private fun PasswordVisibilityButton(
    visible: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    val colors = SegundoAireTheme.colors
    val source = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(ButtonMinSize)
            .focusRing(source, SegundoAireTheme.shapes.radiusFull)
            .clickable(
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onToggle
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (visible) SegundoAireIcons.EyeOff else SegundoAireIcons.Eye,
            contentDescription = stringResource(
                if (visible) R.string.text_field_hide_password else R.string.text_field_show_password
            ),
            tint = if (enabled) colors.onBackground else colors.disabledContent,
            modifier = Modifier.size(FieldIconSize)
        )
    }
}

@Composable
private fun GlassTextFieldSample() {
    val spacing = SegundoAireTheme.spacing
    Column(
        modifier = Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(spacing.space6),
        verticalArrangement = Arrangement.spacedBy(spacing.space4)
    ) {
        GlassTextField(
            value = "",
            onValueChange = {},
            label = stringResource(R.string.auth_email_label),
            placeholder = stringResource(R.string.auth_email_placeholder)
        )
        GlassTextField(
            value = stringResource(R.string.app_name),
            onValueChange = {},
            label = stringResource(R.string.auth_email_label),
            errorMessage = stringResource(R.string.auth_email_error)
        )
        GlassTextField(
            value = stringResource(R.string.app_name),
            onValueChange = {},
            label = stringResource(R.string.auth_password_label),
            isPassword = true
        )
        GlassTextField(
            value = stringResource(R.string.app_name),
            onValueChange = {},
            label = stringResource(R.string.auth_email_label),
            enabled = false
        )
    }
}

@Preview(name = "GlassTextField · claro")
@Composable
private fun GlassTextFieldLightPreview() {
    SegundoAireTheme(darkTheme = false) { GlassTextFieldSample() }
}

@Preview(name = "GlassTextField · oscuro")
@Composable
private fun GlassTextFieldDarkPreview() {
    SegundoAireTheme(darkTheme = true) { GlassTextFieldSample() }
}
