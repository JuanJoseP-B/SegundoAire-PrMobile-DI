package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.SecondaryButton
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.tree.OnboardingHill
import com.segundoaire.presentation.onboarding.tree.OnboardingTree
import com.segundoaire.presentation.onboarding.tree.TreeInput
import com.segundoaire.presentation.onboarding.tree.TreeRules
import com.segundoaire.presentation.onboarding.tree.rememberTreeAnimations
import kotlinx.coroutines.delay

// Pasos que recorre la demostración: semilla, brote, copa, nombre, metas, follaje marchito y compromiso
private val DemoSteps = intArrayOf(0, 1, 2, 3, 4, 5, 10)
private const val DemoStepMillis = 1800L
private val FrameHeight = 400.dp

/**
 * El árbol del onboarding recorriendo sus etapas solo, con un botón para repetirlo. Respeta
 * el interruptor "Reduce motion" del catálogo, igual que en la app.
 */
@Composable
internal fun OnboardingTreeSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_onboarding_tree)) {
        var replays by rememberSaveable { mutableIntStateOf(0) }

        Box(
            Modifier
                .fillMaxWidth()
                .height(FrameHeight)
                .clip(SegundoAireTheme.shapes.radiusLg)
                .background(SegundoAireTheme.colors.background)
        ) {
            // key: repetir arranca el árbol desde la semilla
            key(replays) { TreeDemo() }
        }
        SecondaryButton(
            text = stringResource(R.string.catalog_tree_replay),
            onClick = { replays++ },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TreeDemo() {
    var step by remember { mutableIntStateOf(0) }
    var committed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        DemoSteps.drop(1).forEach {
            delay(DemoStepMillis)
            step = it
        }
        delay(DemoStepMillis)
        committed = true
    }

    val input = TreeInput(
        step = step,
        nameLength = 6,
        picks = if (step >= 4) setOf(0, 2, 3) else emptySet(),
        usageIndex = 4,
        committed = committed
    )
    val animations = rememberTreeAnimations(input)
    // El marco de diseño mide 390 × 844; se muestra su zona del árbol
    Box(
        Modifier
            .requiredSize(TreeRules.FrameWidth.dp, TreeRules.FrameHeight.dp)
            .offset(y = (-190).dp)
    ) {
        OnboardingHill(animations)
        OnboardingTree(
            animations,
            rootProgress = { index -> TreeRules.rootProgress(step, if (committed) 1f else 0.6f, index) }
        )
    }
}
