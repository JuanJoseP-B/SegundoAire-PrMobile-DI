package com.segundoaire.presentation.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.segundoaire.presentation.designsystem.theme.GlassBackdrop
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glassBackdrop
import com.segundoaire.presentation.timer.TimerScreen

/**
 * Contenedor principal: las cuatro pestañas y la barra inferior flotante.
 * [onOpenComponentCatalog] solo se pasa en debug (pulsación larga en el título del Panel).
 */
@Composable
fun MainShell(
    modifier: Modifier = Modifier,
    onOpenComponentCatalog: (() -> Unit)? = null
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.PANEL) }

    MainShellContent(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        modifier = modifier
    ) { tab ->
        when (tab) {
            MainTab.PANEL -> DashboardScreen(onTitleLongClick = onOpenComponentCatalog)
            MainTab.BLOCKS -> TimerScreen()
            MainTab.PROGRESS, MainTab.PROFILE -> ComingSoonTab(tab)
        }
    }
}

/**
 * Estructura del shell sin dependencias de datos: [content] pinta la pestaña.
 * El contenido es la fuente del desenfoque (`hazeSource`) de la barra, que va
 * encima; deja libre abajo el alto de la barra para que nada quede tapado.
 */
@Composable
internal fun MainShellContent(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (MainTab) -> Unit
) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val density = LocalDensity.current
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val slideOffsetPx = with(density) { motion.stepOffset.roundToPx() }
    var barHeightPx by remember { mutableIntStateOf(0) }

    GlassBackdrop {
        Box(modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .glassBackdrop()
                    .background(colors.background)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                            )
                        )
                        .padding(bottom = with(density) { barHeightPx.toDp() }),
                    transitionSpec = {
                        // El tab de índice mayor entra por la derecha; en RTL, por la izquierda
                        val toward = if ((targetState.ordinal > initialState.ordinal) != rtl) 1 else -1
                        val enterFade = fadeIn(motion.nav())
                        val exitFade = fadeOut(motion.nav())
                        // Con reducir movimiento el desplazamiento es 0 y queda solo el fundido
                        if (slideOffsetPx == 0) {
                            enterFade togetherWith exitFade
                        } else {
                            (slideInHorizontally(motion.nav()) { toward * slideOffsetPx } + enterFade) togetherWith
                                (slideOutHorizontally(motion.nav()) { -toward * slideOffsetPx } + exitFade)
                        }
                    },
                    label = "mainTabs"
                ) { tab ->
                    content(tab)
                }
            }
            BottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    // Antes del padding de las barras del sistema, para medir la barra completa
                    .onSizeChanged { barHeightPx = it.height }
                    .navigationBarsPadding()
            )
        }
    }
}

@Composable
private fun MainShellSample() {
    MainShellContent(selectedTab = MainTab.PROGRESS, onTabSelected = {}) { tab ->
        ComingSoonTab(tab)
    }
}

@Preview(name = "MainShell · claro", showSystemUi = true)
@Composable
private fun MainShellLightPreview() {
    SegundoAireTheme(darkTheme = false) { MainShellSample() }
}

@Preview(name = "MainShell · oscuro", showSystemUi = true)
@Composable
private fun MainShellDarkPreview() {
    SegundoAireTheme(darkTheme = true) { MainShellSample() }
}
