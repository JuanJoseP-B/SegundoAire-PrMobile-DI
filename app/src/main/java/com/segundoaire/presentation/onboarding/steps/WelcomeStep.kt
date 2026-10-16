package com.segundoaire.presentation.onboarding.steps

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.OnboardingIcons
import com.segundoaire.presentation.onboarding.StepButton
import com.segundoaire.presentation.onboarding.StepEyebrow
import com.segundoaire.presentation.onboarding.StepSubtitle
import com.segundoaire.presentation.onboarding.StepTextLeft
import com.segundoaire.presentation.onboarding.StepTitle
import com.segundoaire.presentation.onboarding.tree.TreeRules

private class WelcomeCopy(
    @StringRes val eyebrow: Int,
    @StringRes val title: Int,
    @StringRes val body: Int
)

private val WelcomePages = listOf(
    WelcomeCopy(R.string.onb_welcome_1_eyebrow, R.string.onb_welcome_1_title, R.string.onb_welcome_1_body),
    WelcomeCopy(R.string.onb_welcome_2_eyebrow, R.string.onb_welcome_2_title, R.string.onb_welcome_2_body),
    WelcomeCopy(R.string.onb_welcome_3_eyebrow, R.string.onb_welcome_3_title, R.string.onb_welcome_3_body)
)

// Los textos van bajo la colina: el suelo está en 470 y el bloque empieza 44 más abajo
private val WelcomeTextTop = (TreeRules.WelcomeGround + 44f).dp
private const val SlideDistance = 24f

/** Los tres textos de la bienvenida: el de [page] se queda y los demás se deslizan y se funden. */
@Composable
internal fun WelcomeTexts(page: Int, modifier: Modifier = Modifier) {
    val motion = SegundoAireTheme.motion
    WelcomePages.forEachIndexed { index, copy ->
        val current = index == page
        val shift = animateFloatAsState(
            targetValue = if (current) 0f else if (index < page) -SlideDistance else SlideDistance,
            animationSpec = motion.rise(520),
            label = "welcomeShift"
        )
        val visible = animateFloatAsState(
            targetValue = if (current) 1f else 0f,
            animationSpec = motion.fade(300, if (current) 120 else 0),
            label = "welcomeVisible"
        )
        Column(
            modifier
                .offset(x = StepTextLeft, y = WelcomeTextTop)
                .width(342.dp)
                .graphicsLayer {
                    translationX = shift.value * density
                    alpha = visible.value
                }
                .then(if (current) Modifier else Modifier.clearAndSetSemantics { }),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            StepEyebrow(stringResource(copy.eyebrow), leadingIcon = OnboardingIcons.Leaf)
            Spacer(Modifier.height(14.dp))
            StepTitle(stringResource(copy.title), textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            StepSubtitle(
                stringResource(copy.body),
                modifier = Modifier.widthIn(max = 300.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun WelcomeContent(page: Int, onNext: () -> Unit) {
    StepButton(
        text = stringResource(if (page >= WelcomePages.lastIndex) R.string.onb_start else R.string.onb_next),
        onClick = onNext
    )
}
