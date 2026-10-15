package com.segundoaire.presentation.tutorial

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.components.PrimaryButton
import com.segundoaire.presentation.designsystem.components.QuestionScaffold
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass
import kotlinx.coroutines.launch

private val CardIconBoxSize = 72.dp
private val CardIconSize = 32.dp
private val DotSize = 8.dp
private val DotSelectedWidth = 24.dp

private class TutorialCardContent(
    val icon: ImageVector,
    @StringRes val textRes: Int
)

// Qué hace la app, el GlassWall, Mindful vs Strict, los bloques y el progreso
private val TutorialCards by lazy {
    listOf(
        TutorialCardContent(SegundoAireIcons.Device, R.string.tutorial_card_what),
        TutorialCardContent(SegundoAireIcons.Eye, R.string.tutorial_card_glasswall),
        TutorialCardContent(SegundoAireIcons.Check, R.string.tutorial_card_rigor),
        TutorialCardContent(SegundoAireIcons.Blocks, R.string.tutorial_card_blocks),
        TutorialCardContent(SegundoAireIcons.Progress, R.string.tutorial_card_progress)
    )
}

/**
 * Tutorial de cinco tarjetas. Solo se abre desde Ajustes: nunca aparece solo.
 * El botón avanza de tarjeta y, en la última, pasa a "Listo" y llama a [onDone].
 * Sin estado de dominio: la tarjeta visible vive en el [PagerState].
 */
@Composable
fun TutorialScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { TutorialCards.size })
    val motion = SegundoAireTheme.motion
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == TutorialCards.lastIndex

    QuestionScaffold(
        action = {
            PrimaryButton(
                text = stringResource(if (isLast) R.string.tutorial_done else R.string.tutorial_next),
                onClick = {
                    if (isLast) {
                        onDone()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                page = pagerState.currentPage + 1,
                                animationSpec = motion.step()
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        modifier = modifier,
        onBack = onBack,
        scrollContent = false
    ) {
        TutorialPager(pagerState = pagerState)
    }
}

@Composable
private fun ColumnScope.TutorialPager(pagerState: PagerState) {
    val spacing = SegundoAireTheme.spacing
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        pageSpacing = spacing.space4
    ) { page ->
        TutorialCard(
            content = TutorialCards[page],
            modifier = Modifier.fillMaxSize()
        )
    }
    PageDots(
        count = TutorialCards.size,
        selected = pagerState.currentPage,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(top = spacing.space5)
    )
}

@Composable
private fun TutorialCard(content: TutorialCardContent, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    GlassCard(
        modifier = modifier,
        level = GlassLevel.Level2,
        contentPadding = PaddingValues(spacing.space6),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // El contenido va al centro de la tarjeta, que ocupa todo el alto de la página
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.space6, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(CardIconBoxSize)
                    .glass(GlassLevel.Level1, SegundoAireTheme.shapes.radiusFull),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = content.icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(CardIconSize)
                )
            }
            Text(
                text = stringResource(content.textRes),
                style = SegundoAireTheme.typography.bodyLg,
                color = colors.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Puntos de la tarjeta actual: el seleccionado se ensancha y se tiñe con `Motion.toggle`. */
@Composable
private fun PageDots(count: Int, selected: Int, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val description = stringResource(R.string.tutorial_page_description, selected + 1, count)
    Row(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(SegundoAireTheme.spacing.space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val isSelected = index == selected
            val width by animateDpAsState(
                targetValue = if (isSelected) DotSelectedWidth else DotSize,
                animationSpec = motion.toggle(),
                label = "dotWidth"
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) colors.primary else colors.outline,
                animationSpec = motion.toggle(),
                label = "dotColor"
            )
            Box(
                Modifier
                    .width(width)
                    .height(DotSize)
                    .clip(SegundoAireTheme.shapes.radiusFull)
                    .drawBehind { drawRect(color) }
            )
        }
    }
}

@Preview(name = "Tutorial · claro", heightDp = 800)
@Composable
private fun TutorialScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) { TutorialScreen(onDone = {}, onBack = {}) }
}

@Preview(name = "Tutorial · oscuro", heightDp = 800)
@Composable
private fun TutorialScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) { TutorialScreen(onDone = {}, onBack = {}) }
}
