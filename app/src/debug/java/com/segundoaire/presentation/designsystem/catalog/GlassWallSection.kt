package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.domain.model.GlassWallState
import com.segundoaire.presentation.designsystem.components.ActionChip
import com.segundoaire.presentation.designsystem.components.SegmentedSelector
import com.segundoaire.presentation.designsystem.components.TextButton
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.GlassBlurWall
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.overlay.GlassWall
import com.segundoaire.presentation.overlay.GlassWallContent
import com.segundoaire.presentation.overlay.GlassWallPhase

// Lo que le queda al bloque Strict de ejemplo al elegirlo
private const val SampleBlockMillis = 12 * 60_000L

/**
 * El GlassWall sin interceptar ninguna app: la misma coreografía que en el overlay, sobre
 * una app de mentira (colores `app1..5`, sin logos) que se desenfoca como lo haría la
 * ventana. "Simular abrir app" y "Repetir" lo vuelven a abrir desde cero.
 */
@Composable
internal fun GlassWallSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_glass_wall)) {
        val options = listOf(
            stringResource(R.string.catalog_glass_wall_no_block),
            stringResource(R.string.rigor_mindful),
            stringResource(R.string.rigor_strict)
        )
        var selected by rememberSaveable { mutableIntStateOf(1) }
        var opens by rememberSaveable { mutableIntStateOf(1) }
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val state = remember(selected, opens) {
            when (selected) {
                1 -> GlassWallState.Mindful
                2 -> GlassWallState.Strict(System.currentTimeMillis() + SampleBlockMillis)
                else -> GlassWallState.NoBlock
            }
        }

        SegmentedSelector(
            options = options,
            selectedIndex = selected,
            onSelected = {
                selected = it
                opens++
            },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(screenHeight)
                .clip(SegundoAireTheme.shapes.radiusLg)
        ) {
            // Cada apertura entra desde cero, con su cuenta de Mindful
            key(opens) {
                var phase by remember { mutableStateOf(GlassWallPhase.Visible) }
                var backdrop by remember { mutableFloatStateOf(0f) }
                FakeApp(blurred = backdrop, modifier = Modifier.fillMaxSize())
                if (phase != GlassWallPhase.Waiting) {
                    GlassWall(
                        phase = phase,
                        content = GlassWallContent(
                            state = state,
                            appName = stringResource(R.string.catalog_glass_wall_app),
                            minutesToday = 23,
                            weekLeaves = 12,
                            cooldownMinutes = 5
                        ),
                        onBack = { phase = GlassWallPhase.Leaving },
                        onContinue = { phase = GlassWallPhase.Leaving },
                        onIntent = {},
                        onBackdropProgress = { backdrop = it },
                        onHidden = { phase = GlassWallPhase.Waiting },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(SegundoAireTheme.spacing.space2)) {
            ActionChip(
                text = stringResource(R.string.catalog_glass_wall_simulate),
                onClick = { opens++ },
                leadingIcon = SegundoAireIcons.Device
            )
            TextButton(text = stringResource(R.string.catalog_reveal_replay), onClick = { opens++ })
        }
    }
}

/** Una app cualquiera, hecha de bloques de color, que el muro desenfoca al subir. */
@Composable
private fun FakeApp(blurred: Float, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    Column(
        modifier
            .background(colors.surfaceGlass)
            .blur(GlassBlurWall * blurred)
    ) {
        Box(Modifier.fillMaxWidth().height(90.dp).background(colors.app2))
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(colors.app1, colors.app3, colors.app4, colors.app5).forEach {
                Box(Modifier.size(56.dp).background(it, CircleShape))
            }
        }
        Box(
            Modifier
                .padding(horizontal = 14.dp)
                .fillMaxWidth()
                .height(380.dp)
                .clip(SegundoAireTheme.shapes.radiusMd)
                .background(Brush.linearGradient(listOf(colors.app1, colors.app3, colors.app2)))
        )
        Box(
            Modifier
                .padding(14.dp)
                .fillMaxWidth()
                .height(200.dp)
                .clip(SegundoAireTheme.shapes.radiusMd)
                .background(Brush.linearGradient(listOf(colors.app5, colors.app4)))
        )
    }
}
