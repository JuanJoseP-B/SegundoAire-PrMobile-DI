package com.segundoaire.presentation.onboarding.tree

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import com.segundoaire.presentation.designsystem.theme.Motion
import com.segundoaire.presentation.designsystem.theme.SegundoAireMotion
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.tree.TreeGeometry

/** Lo que el árbol necesita saber del onboarding para decidir qué crece. */
@Immutable
internal data class TreeInput(
    val step: Int = 0,
    val nameLength: Int = 0,
    /** Metas elegidas, como posición de la opción (0–4). */
    val picks: Set<Int> = emptySet(),
    /** Rango de uso diario elegido (0 = menos de 2 h … 4 = más de 8 h). */
    val usageIndex: Int = 2,
    val committed: Boolean = false
)

/**
 * Valores animados del árbol y de la colina. Son [State]: quien los dibuja los lee en
 * la fase de dibujo (`drawWithCache`, `graphicsLayer`), así que animarlos no recompone.
 */
@Stable
internal class TreeAnimations(
    val segments: List<State<Float>>,
    val rootsAlpha: State<Float>,
    val blobScale: List<State<Float>>,
    val blobWilt: List<State<Float>>,
    val leafScale: List<State<Float>>,
    val nameLeafScale: List<State<Float>>,
    val flowerProgress: List<State<Float>>,
    val seedScale: State<Float>,
    val rippleAlpha: State<Float>,
    val ripples: List<State<Float>>,
    val sway: State<Float>,
    val translateX: State<Float>,
    val translateY: State<Float>,
    val scale: State<Float>,
    val opacity: State<Float>,
    val hillY: State<Float>,
    val hillOpacity: State<Float>
)

private val RippleDelaysMillis = intArrayOf(0, 1300, 2600)

@Composable
internal fun rememberTreeAnimations(
    input: TreeInput,
    motion: SegundoAireMotion = SegundoAireTheme.motion
): TreeAnimations {
    val step = input.step
    val stage = TreeRules.stage(step)
    val pose = TreeRules.pose(step)
    val wilt = TreeRules.wiltCount(step, input.usageIndex)
    val nameLeaves = TreeRules.nameLeafCount(step, input.nameLength)

    val segments = TreeGeometry.Segments.map { segment ->
        val on = stage >= segment.stage
        animateFloatAsState(if (on) 1f else 0f, motion.grow(on, segment.delayMillis), label = "segment")
    }
    val blobScale = TreeGeometry.Blobs.mapIndexed { i, blob ->
        val on = stage >= blob.stage
        val target = if (!on) 0f else if (TreeRules.isWilted(i, wilt)) WiltedScale else 1f
        val delay = TreeRules.growDelayMillis(on, step, 550, blob.order)
        animateFloatAsState(target, motion.bloom(on, 760, 280, delay), label = "blob")
    }
    val blobWilt = TreeGeometry.Blobs.indices.map { i ->
        animateFloatAsState(
            if (TreeRules.isWilted(i, wilt)) 1f else 0f,
            motion.fade(700),
            label = "wilt"
        )
    }
    val leafScale = TreeGeometry.Leaves.map { leaf ->
        val on = stage >= leaf.stage
        val delay = TreeRules.growDelayMillis(on, step, 700, leaf.order)
        animateFloatAsState(if (on) 1f else 0f, motion.bloom(on, 640, 260, delay), label = "leaf")
    }
    val nameLeafScale = TreeGeometry.NameLeaves.indices.map { i ->
        val on = i < nameLeaves
        animateFloatAsState(if (on) 1f else 0f, motion.bloom(on, 520, 220), label = "nameLeaf")
    }
    val flowerProgress = TreeGeometry.FlowerPositions.indices.map { i ->
        val on = TreeRules.flowerOn(step, i, input.picks, input.committed)
        animateFloatAsState(
            if (on) 1f else 0f,
            motion.bloom(on, 700, 280, TreeRules.flowerDelayMillis(on, i)),
            label = "flower"
        )
    }

    val seedScale = animateFloatAsState(
        if (step == 0) 1f else SeedRestScale,
        motion.bloom(true, 600),
        label = "seed"
    )
    val rippleAlpha = animateFloatAsState(if (step == 0) 1f else 0f, motion.fade(500), label = "rippleAlpha")
    val rootsAlpha = animateFloatAsState(
        if (TreeRules.rootsVisible(step)) 0.9f else 0f,
        motion.fade(400),
        label = "roots"
    )

    val translateX = animateFloatAsState(pose.translateX, motion.settle(), label = "tx")
    val translateY = animateFloatAsState(pose.translateY, motion.settle(), label = "ty")
    val scale = animateFloatAsState(pose.scale, motion.settle(), label = "scale")
    val opacity = animateFloatAsState(pose.opacity, motion.fade(600), label = "opacity")
    val hillY = animateFloatAsState(pose.hillY, motion.settle(), label = "hillY")
    val hillOpacity = animateFloatAsState(pose.hillOpacity, motion.fade(500), label = "hillOpacity")

    // Con reducir movimiento el árbol aparece en su etapa, sin balanceo ni ondas
    val sway: State<Float>
    val ripples: List<State<Float>>
    if (motion.reduced) {
        sway = remember { mutableFloatStateOf(0f) }
        ripples = RippleDelaysMillis.map { remember { mutableFloatStateOf(0f) } }
    } else {
        val transition = rememberInfiniteTransition(label = "tree")
        sway = transition.animateFloat(
            initialValue = -Motion.SwayDegrees,
            targetValue = Motion.SwayDegrees,
            animationSpec = Motion.sway(),
            label = "sway"
        )
        ripples = RippleDelaysMillis.map { delay ->
            transition.animateFloat(0f, 1f, Motion.ripple(delay), label = "ripple")
        }
    }

    return TreeAnimations(
        segments = segments,
        rootsAlpha = rootsAlpha,
        blobScale = blobScale,
        blobWilt = blobWilt,
        leafScale = leafScale,
        nameLeafScale = nameLeafScale,
        flowerProgress = flowerProgress,
        seedScale = seedScale,
        rippleAlpha = rippleAlpha,
        ripples = ripples,
        sway = sway,
        translateX = translateX,
        translateY = translateY,
        scale = scale,
        opacity = opacity,
        hillY = hillY,
        hillOpacity = hillOpacity
    )
}

internal const val WiltedScale = 0.88f
internal const val SeedRestScale = 0.6f
