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
import com.segundoaire.presentation.designsystem.components.SecondaryButton
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.splash.BreathSplash

/**
 * El splash dentro de un marco, con su botón para repetirlo. La versión reducida sale
 * del interruptor "Reduce motion" del catálogo, igual que en la app.
 */
@Composable
internal fun SplashSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_splash)) {
        var replays by rememberSaveable { mutableIntStateOf(0) }
        var finished by remember(replays) { mutableStateOf(false) }

        Box(
            Modifier
                .fillMaxWidth()
                .height(SplashFrameHeight)
                .clip(SegundoAireTheme.shapes.radiusLg)
        ) {
            // key: repetir arranca la animación desde cero
            key(replays) {
                BreathSplash(onFinished = { finished = true })
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
