package com.segundoaire.presentation.onboarding

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.components.LocalEntranceHold
import com.segundoaire.presentation.designsystem.components.staggerIn
import com.segundoaire.presentation.splash.BreathSplashSpec
import com.segundoaire.presentation.designsystem.components.QuestionScaffold
import com.segundoaire.presentation.designsystem.components.stepContentTransform
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

private val PageDotSize = 8.dp

private enum class WelcomeCard(
    @StringRes val eyebrowRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int
) {
    Problem(
        R.string.onboarding_welcome_problem_eyebrow,
        R.string.onboarding_welcome_problem_title,
        R.string.onboarding_welcome_problem_body
    ),
    Pause(
        R.string.onboarding_welcome_pause_eyebrow,
        R.string.onboarding_welcome_pause_title,
        R.string.onboarding_welcome_pause_body
    ),
    Promise(
        R.string.onboarding_welcome_promise_eyebrow,
        R.string.onboarding_welcome_promise_title,
        R.string.onboarding_welcome_promise_body
    )
}

/**
 * Bienvenida: tres tarjetas (el problema, la pausa y la promesa), una a la vez.
 * El botón avanza de tarjeta y, en la última, llama a [onContinue]; volver
 * regresa a la tarjeta anterior.
 */
@Composable
fun WelcomeScreen(
    page: Int,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = SegundoAireTheme.spacing
    val motion = SegundoAireTheme.motion
    val cards = WelcomeCard.entries
    val current = page.coerceIn(0, cards.lastIndex)
    val isLast = current == cards.lastIndex
    val offset = with(LocalDensity.current) { motion.stepOffset.roundToPx() }
    val reading = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1

    BackHandler(enabled = current > 0, onBack = onPreviousPage)

    // Solo entra escalonada si se compone mientras el splash cubre la app (fase 6 de El Respiro)
    val entranceHeld = LocalEntranceHold.current
    val animateEntrance = remember { entranceHeld }

    QuestionScaffold(
        primaryLabel = stringResource(
            if (isLast) R.string.onboarding_welcome_start else R.string.onboarding_continue
        ),
        onPrimary = if (isLast) onContinue else onNextPage,
        modifier = modifier,
        onBack = if (current > 0) onPreviousPage else null
    ) {
        Spacer(Modifier.height(spacing.space16))
        AnimatedContent(
            targetState = current,
            modifier = Modifier.staggerIn(animateEntrance, index = 0, stepMillis = BreathSplashSpec.EntranceStaggerMillis),
            transitionSpec = {
                val direction = if (targetState > initialState) reading else -reading
                stepContentTransform(motion, offset, direction)
            },
            label = "welcomeCard"
        ) { index ->
            WelcomeCardContent(cards[index])
        }
        Spacer(Modifier.height(spacing.space6))
        PageDots(
            current = current,
            total = cards.size,
            modifier = Modifier.staggerIn(animateEntrance, index = 1, stepMillis = BreathSplashSpec.EntranceStaggerMillis)
        )
    }
}

@Composable
private fun WelcomeCardContent(card: WelcomeCard) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val typography = SegundoAireTheme.typography
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(spacing.space6)
    ) {
        OnboardingEyebrow(stringResource(card.eyebrowRes))
        Spacer(Modifier.height(spacing.space3))
        Text(
            text = stringResource(card.titleRes),
            style = typography.headlineMd,
            color = colors.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(spacing.space4))
        Text(
            text = stringResource(card.bodyRes),
            style = typography.bodyLg,
            color = colors.onBackground
        )
    }
}

@Composable
private fun PageDots(current: Int, total: Int, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val description = stringResource(R.string.onboarding_welcome_page, current + 1, total)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(
            SegundoAireTheme.spacing.space2,
            Alignment.CenterHorizontally
        )
    ) {
        repeat(total) { index ->
            val color by animateColorAsState(
                targetValue = if (index == current) colors.primary else colors.outline,
                animationSpec = SegundoAireTheme.motion.toggle(),
                label = "pageDot"
            )
            Box(
                Modifier
                    .size(PageDotSize)
                    .clip(SegundoAireTheme.shapes.radiusFull)
                    .background(color)
            )
        }
    }
}

@Preview(name = "Bienvenida · claro")
@Composable
private fun WelcomeScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) {
        WelcomeScreen(page = 0, onNextPage = {}, onPreviousPage = {}, onContinue = {})
    }
}

@Preview(name = "Bienvenida · oscuro")
@Composable
private fun WelcomeScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        WelcomeScreen(page = 2, onNextPage = {}, onPreviousPage = {}, onContinue = {})
    }
}
