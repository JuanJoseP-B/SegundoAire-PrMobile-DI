package com.segundoaire.presentation.main

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons

/** Pestañas del shell principal, en el orden en que aparecen en la barra. */
enum class MainTab(
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    PANEL(R.string.nav_panel, SegundoAireIcons.Panel),
    BLOCKS(R.string.nav_blocks, SegundoAireIcons.Blocks),
    PROGRESS(R.string.nav_progress, SegundoAireIcons.Progress),
    PROFILE(R.string.nav_profile, SegundoAireIcons.Profile)
}
