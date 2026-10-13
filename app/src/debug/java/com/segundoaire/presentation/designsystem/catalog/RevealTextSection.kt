package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.RevealText
import com.segundoaire.presentation.designsystem.components.TextButton

@Composable
internal fun RevealTextSection() {
    CatalogSection(title = stringResource(R.string.catalog_section_reveal_text)) {
        var replays by remember { mutableIntStateOf(0) }
        key(replays) {
            RevealText(text = stringResource(R.string.catalog_reveal_sample))
        }
        TextButton(
            text = stringResource(R.string.catalog_reveal_replay),
            onClick = { replays++ }
        )
    }
}
