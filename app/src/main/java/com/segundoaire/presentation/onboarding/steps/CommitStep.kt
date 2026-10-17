package com.segundoaire.presentation.onboarding.steps

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.components.focusRing
import com.segundoaire.presentation.designsystem.components.pressScale
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.OnboardingRules
import com.segundoaire.presentation.onboarding.StepButtonHeight
import com.segundoaire.presentation.onboarding.StepButtonTop
import com.segundoaire.presentation.onboarding.StepContentSide
import com.segundoaire.presentation.onboarding.StepHeader
import java.text.DateFormat
import java.util.Date

/**
 * Estado de "mantener presionado" compartido con el árbol: sus raíces se dibujan con [hold].
 * Se lee solo al dibujar; [holding] no hace recomponer a nadie.
 */
@Stable
internal class HoldController {
    var hold by mutableFloatStateOf(0f)
    var holding: Boolean = false
    var marks by mutableIntStateOf(0)

    fun reset() {
        hold = 0f
        holding = false
        marks = 0
    }
}

@Composable
internal fun CommitText(step: Int, total: Int) {
    StepHeader(
        eyebrow = stringResource(R.string.onb_step_of, step, total),
        title = stringResource(R.string.onb_commit_title)
    )
}

/**
 * Compromiso: hay que mantener presionado 1,5 s y las raíces del árbol se dibujan con el
 * progreso; al soltar se vacía en 300 ms. [onMark] avisa al cruzar el 33 % y el 66 %, y
 * [onCommit] al llegar al 100 %. Con TalkBack, la acción "Comprometerme" confirma sin mantener.
 */
@Composable
internal fun CommitContent(
    active: Boolean,
    name: String,
    committed: Boolean,
    controller: HoldController,
    onMark: () -> Unit,
    onCommit: () -> Unit
) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val locale = LocalConfiguration.current.locales[0]
    val today = remember(locale) { DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date()) }
    val source = remember { MutableInteractionSource() }

    LaunchedEffect(active, committed) {
        if (!active || committed) return@LaunchedEffect
        var last = 0L
        while (true) {
            androidx.compose.runtime.withFrameNanos { now ->
                // Un salto largo (la app estuvo en pausa) no debe llenar el botón de golpe
                val dt = if (last == 0L) 0f else ((now - last) / 1_000_000f).coerceAtMost(50f)
                last = now
                val next = OnboardingRules.advanceHold(controller.hold, controller.holding, dt)
                controller.hold = next
                val reached = OnboardingRules.marksReached(next)
                if (reached > controller.marks) {
                    controller.marks = reached
                    onMark()
                }
                if (next >= 1f) {
                    controller.holding = false
                    onCommit()
                }
            }
            if (controller.hold >= 1f) break
        }
    }

    GlassCard(
        modifier = Modifier
            .offset(x = StepContentSide, y = 190.dp)
            .width(358.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp)
    ) {
        Text(
            text = stringResource(R.string.onb_commit_quote, name),
            style = typography.serif(size = 22, lineHeight = 30, weight = FontWeight.W400, italic = true),
            color = colors.onBackground
        )
        Text(
            text = stringResource(R.string.onb_commit_signature, name, today),
            modifier = Modifier.offset(y = 16.dp),
            style = typography.bodyMd.copy(fontSize = 13.sp),
            color = colors.onBackgroundMuted
        )
    }

    val description = stringResource(R.string.onb_commit_description)
    val action = stringResource(R.string.onb_commit_action)
    val shape = SegundoAireTheme.shapes.radiusFull
    Box(
        Modifier
            .offset(x = StepContentSide, y = StepButtonTop)
            .width(358.dp)
            .height(StepButtonHeight)
            .pressScale(source, enabled = !committed)
            .focusRing(source, shape)
            .clip(shape)
            .drawBehind {
                drawRect(colors.primary)
                drawRect(colors.glow, size = Size(size.width * controller.hold.coerceIn(0f, 1f), size.height))
            }
            .pointerInput(committed) {
                if (committed) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val press = PressInteraction.Press(down.position)
                    source.tryEmit(press)
                    controller.holding = true
                    waitForUpOrCancellation()
                    controller.holding = false
                    source.tryEmit(PressInteraction.Release(press))
                }
            }
            .semantics(mergeDescendants = true) {
                contentDescription = description
                role = Role.Button
                onClick(label = action) {
                    if (!committed) onCommit()
                    true
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(if (committed) R.string.onb_commit_done else R.string.onb_commit_hold),
            style = typography.labelLg,
            color = colors.onPrimary
        )
    }
}
