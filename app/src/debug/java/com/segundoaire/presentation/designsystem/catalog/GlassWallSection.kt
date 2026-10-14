package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.domain.model.GlassWallState
import com.segundoaire.domain.model.Goal
import com.segundoaire.presentation.designsystem.components.SegmentedSelector
import com.segundoaire.presentation.designsystem.components.TextButton
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.overlay.GlassWall
import com.segundoaire.presentation.overlay.GlassWallPhase

// Lo que le queda al bloque Strict de ejemplo al elegirlo
private const val SampleBlockMillis = 12 * 60_000L

/**
 * El GlassWall sin interceptar ninguna app: mismo contenido y misma animación que
 * en el overlay, a la altura de la pantalla. Aquí el fondo es el del catálogo y el
 * vidrio desenfoca con Haze; el blur de ventana solo existe en el overlay real.
 */
@Composable
internal fun GlassWallSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_glass_wall)) {
        val options = listOf(
            stringResource(R.string.catalog_glass_wall_no_block),
            stringResource(R.string.rigor_mindful),
            stringResource(R.string.rigor_strict)
        )
        var selected by rememberSaveable { mutableIntStateOf(0) }
        var phase by remember { mutableStateOf(GlassWallPhase.Visible) }
        val state = remember(selected) {
            when (selected) {
                1 -> GlassWallState.Mindful
                2 -> GlassWallState.Strict(System.currentTimeMillis() + SampleBlockMillis)
                else -> GlassWallState.NoBlock
            }
        }
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp

        SegmentedSelector(
            options = options,
            selectedIndex = selected,
            onSelected = { selected = it },
            modifier = Modifier.fillMaxWidth()
        )
        // Cada variante entra desde cero, y con ella la cuenta de Mindful
        key(selected) {
            GlassWall(
                phase = phase,
                state = state,
                name = stringResource(R.string.catalog_sample_name),
                goal = Goal.SLEEP,
                // En el overlay ambas acciones lo cierran; aquí vuelve a entrar al terminar de salir
                onExit = { phase = GlassWallPhase.Leaving },
                onContinue = { phase = GlassWallPhase.Leaving },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight)
                    .clip(SegundoAireTheme.shapes.radiusLg),
                onHidden = { phase = GlassWallPhase.Visible }
            )
        }
        TextButton(
            text = stringResource(R.string.catalog_reveal_replay),
            onClick = { phase = GlassWallPhase.Leaving }
        )
    }
}
