package com.segundoaire.presentation.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.presentation.theme.SegundoAireTheme
import com.segundoaire.presentation.timer.TimerScreen

private enum class MainTab(val labelRes: Int) {
    PANEL(R.string.nav_panel),
    BLOCKS(R.string.nav_blocks)
}

/** Contenedor principal: barra de navegación inferior que alterna Panel y Bloques. */
@Composable
fun MainScreen() {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.PANEL) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = SegundoAireTheme.glass.surface) {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { TabIcon(tab) },
                        label = { Text(stringResource(tab.labelRes)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (selectedTab) {
            MainTab.PANEL -> DashboardScreen(modifier = contentModifier)
            MainTab.BLOCKS -> TimerScreen(modifier = contentModifier)
        }
    }
}

/** Iconos dibujados a mano para no depender de material-icons-extended. */
@Composable
private fun TabIcon(tab: MainTab) {
    val color = LocalContentColor.current
    Canvas(Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.dp.toPx())
        when (tab) {
            MainTab.PANEL -> {
                val cell = Size(size.width * 0.38f, size.height * 0.38f)
                val radius = CornerRadius(3.dp.toPx())
                listOf(
                    Offset(2.dp.toPx(), 2.dp.toPx()),
                    Offset(size.width - cell.width - 2.dp.toPx(), 2.dp.toPx()),
                    Offset(2.dp.toPx(), size.height - cell.height - 2.dp.toPx()),
                    Offset(size.width - cell.width - 2.dp.toPx(), size.height - cell.height - 2.dp.toPx())
                ).forEach { drawRoundRect(color, it, cell, radius, stroke) }
            }
            MainTab.BLOCKS -> {
                drawCircle(color, radius = size.minDimension / 2 - 2.dp.toPx(), style = stroke)
                drawLine(color, center, Offset(center.x, 6.dp.toPx()), strokeWidth = 2.dp.toPx())
                drawLine(
                    color, center, Offset(size.width - 7.dp.toPx(), center.y),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}
