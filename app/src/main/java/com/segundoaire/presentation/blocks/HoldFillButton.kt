package com.segundoaire.presentation.blocks

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.focusRing
import com.segundoaire.presentation.designsystem.components.pressScale
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

private val ButtonHeight = 52.dp
private const val FillAlpha = 0.55f

/**
 * Botón de 52 dp que hay que mantener presionado 1,5 s (`Motion.hold`): un relleno
 * [fill] al 55 % lo cruza de lado a lado y, al completarse, llama a [onCommit]. Soltar
 * antes lo vacía en 300 ms (`Motion.holdRelease`). Con [onTap] no hace falta mantener:
 * un toque basta (el bloque ya está en curso). Con TalkBack o acceso por interruptores,
 * activarlo confirma directamente.
 */
@Composable
internal fun HoldFillButton(
    text: String,
    onCommit: () -> Unit,
    container: Color,
    content: Color,
    fill: Color,
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)? = null,
    accessibilityLabel: String = text
) {
    val motion = SegundoAireTheme.motion
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val progress = remember { Animatable(0f) }
    val commit by rememberUpdatedState(onCommit)
    val holding = onTap == null

    LaunchedEffect(pressed, holding) {
        if (pressed && holding) {
            progress.animateTo(1f, motion.hold())
            progress.snapTo(0f)
            commit()
        } else {
            progress.animateTo(0f, motion.holdRelease())
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonHeight)
            .pressScale(source)
            .focusRing(source, CircleShape)
            .clip(CircleShape)
            .background(container)
            .drawBehind {
                val width = size.width * progress.value
                if (width > 0f) {
                    drawRect(
                        color = fill.copy(alpha = FillAlpha),
                        topLeft = Offset(if (rtl) size.width - width else 0f, 0f),
                        size = Size(width, size.height)
                    )
                }
            }
            // Va antes del clickable para reemplazar su acción de accesibilidad
            .semantics {
                contentDescription = accessibilityLabel
                onClick {
                    (onTap ?: commit)()
                    true
                }
            }
            .clickable(
                interactionSource = source,
                indication = null,
                role = Role.Button,
                onClick = { onTap?.invoke() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SegundoAireTheme.typography.labelLg,
            color = content,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = SegundoAireTheme.spacing.space4)
        )
    }
}

@Composable
private fun HoldFillButtonSample() {
    val colors = SegundoAireTheme.colors
    Column(Modifier.background(colors.background).padding(SegundoAireTheme.spacing.space4)) {
        HoldFillButton(
            text = stringResource(R.string.blocks_hold),
            onCommit = {},
            container = colors.primary,
            content = colors.onPrimary,
            fill = colors.glow
        )
    }
}

@Preview(name = "HoldFillButton · claro")
@Composable
private fun HoldFillButtonLightPreview() {
    SegundoAireTheme(darkTheme = false) { HoldFillButtonSample() }
}

@Preview(name = "HoldFillButton · oscuro")
@Composable
private fun HoldFillButtonDarkPreview() {
    SegundoAireTheme(darkTheme = true) { HoldFillButtonSample() }
}
