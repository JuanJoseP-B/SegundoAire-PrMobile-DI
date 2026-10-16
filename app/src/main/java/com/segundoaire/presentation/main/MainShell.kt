package com.segundoaire.presentation.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.segundoaire.presentation.blocks.TimerRoute
import com.segundoaire.presentation.designsystem.theme.GlassBackdrop
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glassBackdrop
import com.segundoaire.presentation.panel.PanelRoute
import com.segundoaire.presentation.profile.ProfileRoute
import com.segundoaire.presentation.progress.ProgressRoute

/**
 * Contenedor principal: las cuatro pestañas y la barra inferior flotante.
 * [onEditApps] abre la selección de apps desde el Panel y [onOpenTutorial] el
 * tutorial desde Ajustes. [onResetCompleted] vuelve a la bienvenida tras reiniciar
 * la app. [onOpenComponentCatalog]
 * solo se pasa en debug (pulsación larga en el saludo del Panel).
 */
@Composable
fun MainShell(
    onEditApps: () -> Unit,
    onOpenTutorial: () -> Unit,
    onResetCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenComponentCatalog: (() -> Unit)? = null,
    viewModel: MainShellViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val toast by viewModel.toast.collectAsStateWithLifecycle()

    MainShellContent(
        selectedTab = selectedTab,
        onTabSelected = viewModel::selectTab,
        modifier = modifier,
        overlay = { barPadding ->
            ShellToast(
                message = toast,
                onDone = viewModel::onToastDone,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(barPadding)
                    .padding(bottom = SegundoAireTheme.spacing.space4)
            )
        }
    ) { tab, contentPadding ->
        when (tab) {
            MainTab.PANEL -> PanelRoute(
                onOpenTab = viewModel::selectTab,
                // El modo enfoque llega con Bloques; hasta entonces el bloque se ve en su pestaña
                onOpenActiveBlock = { viewModel.selectTab(MainTab.BLOCKS) },
                onToast = viewModel::showToast,
                contentPadding = contentPadding,
                onTitleLongClick = onOpenComponentCatalog
            )
            MainTab.BLOCKS -> TimerRoute(contentPadding = contentPadding)
            MainTab.PROGRESS -> ProgressRoute(contentPadding = contentPadding)
            MainTab.PROFILE -> ProfileRoute(
                contentPadding = contentPadding,
                onOpenTutorial = onOpenTutorial,
                onResetCompleted = onResetCompleted
            )
        }
    }
}

/**
 * Estructura del shell sin dependencias de datos: [content] pinta la pestaña y
 * [overlay] lo que flota encima (el aviso breve), con el alto de la barra como padding.
 * Al cambiar de pestaña, la que entra se desplaza 24 dp en la dirección del tab
 * (`Motion.page`) mientras ambas se funden (`Motion.pageFade`).
 * El contenido es la fuente del desenfoque (`hazeSource`) de la barra, que va
 * encima y por eso pasa por detrás de ella. [content] recibe como padding el
 * alto de la barra para que su último elemento pueda quedar libre.
 */
@Composable
internal fun MainShellContent(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.(barPadding: PaddingValues) -> Unit = {},
    content: @Composable (MainTab, PaddingValues) -> Unit
) {
    val colors = SegundoAireTheme.colors
    val motion = SegundoAireTheme.motion
    val density = LocalDensity.current
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val slideOffsetPx = with(density) { motion.stepOffset.roundToPx() }
    var barHeightPx by remember { mutableIntStateOf(0) }
    val barPadding = PaddingValues(bottom = with(density) { barHeightPx.toDp() })

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
                        ),
                    transitionSpec = {
                        // El tab de índice mayor entra por la derecha; en RTL, por la izquierda
                        val toward = if ((targetState.ordinal > initialState.ordinal) != rtl) 1 else -1
                        val enterFade = fadeIn(motion.pageFade())
                        val exitFade = fadeOut(motion.pageFade())
                        // Con reducir movimiento el desplazamiento es 0 y queda solo el fundido
                        if (slideOffsetPx == 0) {
                            enterFade togetherWith exitFade
                        } else {
                            (slideInHorizontally(motion.page()) { toward * slideOffsetPx } + enterFade) togetherWith
                                (slideOutHorizontally(motion.page()) { -toward * slideOffsetPx } + exitFade)
                        }
                    },
                    label = "mainTabs"
                ) { tab ->
                    content(tab, barPadding)
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
            overlay(barPadding)
        }
    }
}

@Composable
private fun MainShellSample() {
    MainShellContent(selectedTab = MainTab.PROGRESS, onTabSelected = {}) { tab, contentPadding ->
        ComingSoonTab(tab, modifier = Modifier.padding(contentPadding))
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
