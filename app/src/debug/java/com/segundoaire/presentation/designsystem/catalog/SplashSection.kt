package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.domain.model.StartDestination
import com.segundoaire.presentation.designsystem.components.SecondaryButton
import com.segundoaire.presentation.designsystem.components.SegmentedSelector
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.splash.SplashOverlay
import com.segundoaire.presentation.splash.SplashTimeline

/**
 * El splash dentro de un marco, con su botón para repetirlo. La versión reducida sale
 * del interruptor "Reduce motion" del catálogo, igual que en la app.
 */
@Composable
internal fun SplashSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_splash)) {
        var selected by rememberSaveable { mutableIntStateOf(0) }
        var replays by rememberSaveable { mutableIntStateOf(0) }
        var finished by remember(selected, replays) { mutableStateOf(false) }
        val reduced = SegundoAireTheme.motion.reduced
        val destination = if (selected == 0) StartDestination.ONBOARDING else StartDestination.MAIN
        val variant = SplashTimeline.variantFor(destination, reduced)

        SegmentedSelector(
            options = listOf(
                stringResource(R.string.catalog_splash_full),
                stringResource(R.string.catalog_splash_short)
            ),
            selectedIndex = selected,
            onSelected = { selected = it },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(SplashFrameHeight)
                .clip(SegundoAireTheme.shapes.radiusLg)
        ) {
            // key: cambiar la variante o repetir arranca la animación desde cero
            key(variant, replays) {
                SplashOverlay(variant = variant, onFinished = { finished = true })
            }
        }
        CatalogCaption(
            if (finished) stringResource(R.string.catalog_splash_done)
            else stringResource(R.string.catalog_splash_reduced_note)
        )
        SecondaryButton(
            text = stringResource(R.string.catalog_splash_replay),
            onClick = { replays++ },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private val SplashFrameHeight = 360.dp
