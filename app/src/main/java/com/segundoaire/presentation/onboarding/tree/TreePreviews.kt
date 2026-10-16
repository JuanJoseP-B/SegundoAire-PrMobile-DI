package com.segundoaire.presentation.onboarding.tree

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

@Composable
private fun TreeStage(input: TreeInput, darkTheme: Boolean = false, holdProgress: Float = 0f) {
    SegundoAireTheme(darkTheme = darkTheme) {
        val animations = rememberTreeAnimations(input)
        Box(
            Modifier
                .size(TreeRules.FrameWidth.dp, TreeRules.FrameHeight.dp)
                .background(SegundoAireTheme.colors.background)
        ) {
            OnboardingHill(animations)
            OnboardingTree(animations, rootProgress = { TreeRules.rootProgress(input.step, holdProgress, it) })
        }
    }
}

@Preview(name = "Árbol · 0 semilla", widthDp = 390, heightDp = 844)
@Composable
private fun TreeSeedPreview() = TreeStage(TreeInput(step = 0))

@Preview(name = "Árbol · 1 brote", widthDp = 390, heightDp = 844)
@Composable
private fun TreeSproutPreview() = TreeStage(TreeInput(step = 1))

@Preview(name = "Árbol · 2 copa", widthDp = 390, heightDp = 844)
@Composable
private fun TreeCanopyPreview() = TreeStage(TreeInput(step = 2))

@Preview(name = "Árbol · 3 nombre", widthDp = 390, heightDp = 844)
@Composable
private fun TreeNamePreview() = TreeStage(TreeInput(step = 3, nameLength = 6))

@Preview(name = "Árbol · 4 metas con flores", widthDp = 390, heightDp = 844)
@Composable
private fun TreeGoalsPreview() = TreeStage(TreeInput(step = 4, nameLength = 6, picks = setOf(0, 2)))

@Preview(name = "Árbol · 5 follaje marchito", widthDp = 390, heightDp = 844)
@Composable
private fun TreeWiltPreview() = TreeStage(TreeInput(step = 5, nameLength = 6, usageIndex = 4))

@Preview(name = "Árbol · 10 raíces a medias", widthDp = 390, heightDp = 844)
@Composable
private fun TreeRootsPreview() = TreeStage(TreeInput(step = 10, nameLength = 6, picks = setOf(1)), holdProgress = 0.6f)

@Preview(name = "Árbol · 10 comprometido", widthDp = 390, heightDp = 844)
@Composable
private fun TreeCommittedPreview() =
    TreeStage(TreeInput(step = 10, nameLength = 6, committed = true), holdProgress = 1f)

@Preview(name = "Árbol · copa · oscuro", widthDp = 390, heightDp = 844)
@Composable
private fun TreeCanopyDarkPreview() = TreeStage(TreeInput(step = 2), darkTheme = true)
