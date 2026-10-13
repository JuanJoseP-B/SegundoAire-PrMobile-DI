package com.segundoaire.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.components.EmptyState
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

/** Pestaña que aún no tiene contenido: Progreso y Perfil hasta sus fases. */
@Composable
fun ComingSoonTab(tab: MainTab, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EmptyState(
            title = stringResource(R.string.coming_soon_title),
            message = stringResource(R.string.coming_soon_message),
            icon = tab.icon
        )
    }
}

@Composable
private fun ComingSoonTabSample() {
    Box(Modifier.background(SegundoAireTheme.colors.background)) {
        ComingSoonTab(tab = MainTab.PROGRESS)
    }
}

@Preview(name = "ComingSoonTab · claro")
@Composable
private fun ComingSoonTabLightPreview() {
    SegundoAireTheme(darkTheme = false) { ComingSoonTabSample() }
}

@Preview(name = "ComingSoonTab · oscuro")
@Composable
private fun ComingSoonTabDarkPreview() {
    SegundoAireTheme(darkTheme = true) { ComingSoonTabSample() }
}
