package com.segundoaire.presentation.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.SegundoAireColors
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/** Relleno del [PrimaryButton]: `primary` para la acción, `secondary` para terminarla. */
enum class PrimaryButtonTone { Primary, Secondary }

/**
 * Acción principal de la pantalla. Si [text], [icon] o [tone] cambian, el botón
 * hace el morph con `Motion.toggle`: el relleno pasa de un tono al otro y el
 * contenido se funde. Con reducir movimiento todo es un fundido de 150 ms.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tone: PrimaryButtonTone = PrimaryButtonTone.Primary,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null
) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val hovered by source.collectIsHoveredAsState()

    val transition = updateTransition(
        targetState = PrimaryButtonState(tone, enabled, pressed, hovered),
        label = "PrimaryButton"
    )
    // El cambio de tono es el morph; el resto es la respuesta al gesto.
    // animateColor espera un transitionSpec @Composable: sin la anotación el
    // compilador no avisa, pero en ejecución el cast a Function3 falla
    val colorSpec: @Composable Transition.Segment<PrimaryButtonState>.() -> FiniteAnimationSpec<Color> = {
        if (initialState.tone != targetState.tone) motion.toggle() else motion.press()
    }
    val container by transition.animateColor(colorSpec, label = "container") { colors.container(it) }
    val content by transition.animateColor(colorSpec, label = "content") { colors.content(it) }

    ButtonLayout(
        onClick = onClick,
        enabled = enabled,
        interactionSource = source,
        surface = Modifier
            .clip(SegundoAireTheme.shapes.radiusFull)
            .drawBehind { drawRect(container) },
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = PrimaryButtonLabel(text, icon),
            transitionSpec = {
                fadeIn(motion.toggle()) togetherWith fadeOut(motion.toggle()) using
                    SizeTransform(clip = false) { _, _ -> motion.toggle() }
            },
            contentAlignment = Alignment.Center,
            label = "PrimaryButtonLabel"
        ) { label ->
            ButtonLabel(text = label.text, icon = label.icon, color = content)
        }
    }
}

/**
 * Botón del bloque de enfoque: "Iniciar bloque" en `primary` con play y, con
 * el bloque [active], "Terminar bloque" en `secondary` con stop.
 */
@Composable
fun BlockButton(
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null
) {
    PrimaryButton(
        text = stringResource(if (active) R.string.timer_stop else R.string.timer_start),
        onClick = onClick,
        modifier = modifier,
        icon = if (active) SegundoAireIcons.Stop else SegundoAireIcons.Play,
        tone = if (active) PrimaryButtonTone.Secondary else PrimaryButtonTone.Primary,
        enabled = enabled,
        interactionSource = interactionSource
    )
}

private data class PrimaryButtonState(
    val tone: PrimaryButtonTone,
    val enabled: Boolean,
    val pressed: Boolean,
    val hovered: Boolean
)

private data class PrimaryButtonLabel(val text: String, val icon: ImageVector?)

private fun SegundoAireColors.container(state: PrimaryButtonState): Color = when {
    !state.enabled -> disabledContainer
    state.tone == PrimaryButtonTone.Secondary ->
        // secondary no tiene tokens de hover/pressed: usa la capa de estado
        if (state.pressed || state.hovered) stateLayer.compositeOver(secondary) else secondary
    state.pressed -> primaryPressed
    state.hovered -> primaryHover
    else -> primary
}

private fun SegundoAireColors.content(state: PrimaryButtonState): Color = when {
    !state.enabled -> disabledContent
    state.tone == PrimaryButtonTone.Secondary -> onSecondary
    else -> onPrimary
}

@Composable
private fun PrimaryButtonSample() {
    val spacing = SegundoAireTheme.spacing
    Column(
        modifier = Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(spacing.space6),
        verticalArrangement = Arrangement.spacedBy(spacing.space4)
    ) {
        PrimaryButton(text = stringResource(R.string.onboarding_continue), onClick = {})
        PrimaryButton(text = stringResource(R.string.onboarding_continue), onClick = {}, enabled = false)
        BlockButton(active = false, onClick = {})
        BlockButton(active = true, onClick = {})
    }
}

@Preview(name = "PrimaryButton · claro")
@Composable
private fun PrimaryButtonLightPreview() {
    SegundoAireTheme(darkTheme = false) { PrimaryButtonSample() }
}

@Preview(name = "PrimaryButton · oscuro")
@Composable
private fun PrimaryButtonDarkPreview() {
    SegundoAireTheme(darkTheme = true) { PrimaryButtonSample() }
}
