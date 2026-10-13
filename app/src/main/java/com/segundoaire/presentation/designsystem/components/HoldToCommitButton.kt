package com.segundoaire.presentation.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/**
 * Botón de compromiso: hay que mantenerlo presionado 1.5 s (`Motion.hold`,
 * tiempo real) mientras `primary` lo va llenando; al completarse vibra y llama
 * a [onCommit]. Soltar antes lo vacía. Con TalkBack o acceso por interruptores,
 * donde no se puede mantener presionado, activar el botón confirma directamente.
 */
@Composable
fun HoldToCommitButton(
    text: String,
    onCommit: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val motion = SegundoAireTheme.motion
    val shape = SegundoAireTheme.shapes.radiusFull
    val haptics = LocalHapticFeedback.current
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val currentOnCommit by rememberUpdatedState(onCommit)
    val progress = remember { Animatable(0f) }

    LaunchedEffect(pressed, enabled) {
        if (pressed && enabled) {
            progress.animateTo(1f, motion.hold())
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            currentOnCommit()
        } else {
            progress.animateTo(0f, motion.press())
        }
    }

    Box(
        modifier = modifier
            .pressScale(source, enabled)
            .focusRing(source, shape)
            .clip(shape)
            .background(if (enabled) colors.secondary else colors.disabledContainer)
            // Va antes del clickable para reemplazar su acción de accesibilidad
            .semantics {
                onClick {
                    currentOnCommit()
                    true
                }
            }
            .clickable(
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = {}
            )
            .defaultMinSize(minWidth = ButtonMinSize, minHeight = ButtonMinSize),
        contentAlignment = Alignment.Center
    ) {
        val labelPadding = Modifier.padding(horizontal = spacing.space6, vertical = spacing.space3)
        Box(labelPadding) {
            ButtonLabel(
                text = text,
                icon = null,
                color = if (enabled) colors.onSecondary else colors.disabledContent
            )
        }
        // La misma etiqueta sobre primary, recortada al avance: el texto
        // conserva el contraste a ambos lados del borde del relleno
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawWithContent {
                    val filled = size.width * progress.value
                    clipRect(
                        left = if (rtl) size.width - filled else 0f,
                        right = if (rtl) size.width else filled
                    ) {
                        this@drawWithContent.drawContent()
                    }
                }
                .background(colors.primary),
            contentAlignment = Alignment.Center
        ) {
            Box(labelPadding) {
                ButtonLabel(text = text, icon = null, color = colors.onPrimary)
            }
        }
    }
}

@Composable
private fun HoldToCommitButtonSample() {
    val spacing = SegundoAireTheme.spacing
    Column(
        modifier = Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(spacing.space6),
        verticalArrangement = Arrangement.spacedBy(spacing.space4)
    ) {
        HoldToCommitButton(
            text = stringResource(R.string.onboarding_continue),
            onCommit = {},
            modifier = Modifier.fillMaxWidth()
        )
        HoldToCommitButton(
            text = stringResource(R.string.onboarding_continue),
            onCommit = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )
    }
}

@Preview(name = "HoldToCommitButton · claro")
@Composable
private fun HoldToCommitButtonLightPreview() {
    SegundoAireTheme(darkTheme = false) { HoldToCommitButtonSample() }
}

@Preview(name = "HoldToCommitButton · oscuro")
@Composable
private fun HoldToCommitButtonDarkPreview() {
    SegundoAireTheme(darkTheme = true) { HoldToCommitButtonSample() }
}
