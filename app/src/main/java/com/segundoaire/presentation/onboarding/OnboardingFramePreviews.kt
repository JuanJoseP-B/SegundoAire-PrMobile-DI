package com.segundoaire.presentation.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.steps.CurtainOverlay
import com.segundoaire.presentation.onboarding.steps.CurtainState
import com.segundoaire.presentation.onboarding.tree.OnboardingHill
import com.segundoaire.presentation.onboarding.tree.OnboardingTree
import com.segundoaire.presentation.onboarding.tree.SceneAtmosphere
import com.segundoaire.presentation.onboarding.tree.TreeInput
import com.segundoaire.presentation.onboarding.tree.rememberTreeAnimations

/** Cielo, colina y árbol, o el telón retirado: el fondo debe tocar los dos bordes laterales. */
@Composable
private fun FramePreview(darkTheme: Boolean, curtain: Boolean) {
    SegundoAireTheme(darkTheme = darkTheme) {
        val animations = rememberTreeAnimations(TreeInput(step = 6, nameLength = 5, picks = setOf(1)))
        OnboardingFrame {
            Box(Modifier.fillMaxSize()) {
                SceneAtmosphere(moment = 2, skyVisible = true, firefliesVisible = true, reducedMotion = true)
                OnboardingHill(animations)
                FrameColumn { OnboardingTree(animations, rootProgress = { 0f }) }
                if (curtain) {
                    CurtainOverlay(state = CurtainState.Rest, done = false, name = "Ana", onFinish = {})
                }
            }
        }
    }
}

@Preview(name = "Marco · 360 dp · claro", widthDp = 360, heightDp = 780)
@Composable
private fun Frame360LightPreview() = FramePreview(darkTheme = false, curtain = false)

@Preview(name = "Marco · 360 dp · oscuro", widthDp = 360, heightDp = 780)
@Composable
private fun Frame360DarkPreview() = FramePreview(darkTheme = true, curtain = false)

@Preview(name = "Marco · 411 dp · claro", widthDp = 411, heightDp = 891)
@Composable
private fun Frame411LightPreview() = FramePreview(darkTheme = false, curtain = false)

@Preview(name = "Marco · 411 dp · oscuro", widthDp = 411, heightDp = 891)
@Composable
private fun Frame411DarkPreview() = FramePreview(darkTheme = true, curtain = false)

@Preview(name = "Marco con telón · 411 dp · claro", widthDp = 411, heightDp = 891)
@Composable
private fun FrameCurtain411Preview() = FramePreview(darkTheme = false, curtain = true)

@Preview(name = "Marco con telón · 360 dp · oscuro", widthDp = 360, heightDp = 780)
@Composable
private fun FrameCurtain360DarkPreview() = FramePreview(darkTheme = true, curtain = true)
