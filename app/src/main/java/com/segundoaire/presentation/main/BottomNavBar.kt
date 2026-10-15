package com.segundoaire.presentation.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.segundoaire.presentation.designsystem.components.focusRing
import com.segundoaire.presentation.designsystem.components.LimitFontScale
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass

// 56 dp de pestaña + 4 dp de margen por lado: 64 dp de barra, con 48 dp o más de área táctil
private val TabMinHeight = 56.dp
private val TabIconSize = 24.dp
private const val IndicatorAlpha = 0.6f
private const val LabelMaxFontScale = 1.3f

/**
 * Barra inferior flotante de DESIGN_SYSTEM.md §5: vidrio `Level3` con `radiusFull`
 * y margen `space4`. El indicador de la pestaña activa se desliza con
 * `Motion.toggle` y el ícono pasa a `primary`. Con reducir movimiento el
 * indicador no se desplaza: se funde en la pestaña nueva. Cada pestaña lleva
 * `Role.Tab` y su etiqueta como `contentDescription`.
 */
@Composable
fun BottomNavBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val motion = SegundoAireTheme.motion
    val shape = SegundoAireTheme.shapes.radiusFull
    val reduced = motion.reduced
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val tabs = MainTab.entries
    val indicatorColor = colors.background.copy(alpha = IndicatorAlpha)
    val position by animateFloatAsState(
        targetValue = selectedTab.ordinal.toFloat(),
        animationSpec = motion.toggle(),
        label = "bottomNavIndicator"
    )

    Row(
        modifier = modifier
            .padding(spacing.space4)
            .fillMaxWidth()
            .glass(GlassLevel.Level3, shape)
            .padding(spacing.space1)
            .drawBehind {
                if (reduced) return@drawBehind
                val tabWidth = size.width / tabs.size
                val start = position * tabWidth
                drawRoundRect(
                    color = indicatorColor,
                    topLeft = Offset(if (rtl) size.width - start - tabWidth else start, 0f),
                    size = Size(tabWidth, size.height),
                    cornerRadius = CornerRadius(size.height / 2)
                )
            }
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            val selected = tab == selectedTab
            val label = stringResource(tab.labelRes)
            val source = remember { MutableInteractionSource() }
            val selection by animateFloatAsState(
                targetValue = if (selected) 1f else 0f,
                animationSpec = motion.toggle(),
                label = "bottomNavSelection"
            )
            val tint = lerp(colors.onBackgroundMuted, colors.primary, selection.coerceIn(0f, 1f))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .focusRing(source, shape)
                    .drawBehind {
                        // Sin desplazamiento, cada pestaña funde su propio relleno
                        if (reduced) {
                            drawRoundRect(
                                color = indicatorColor.copy(
                                    alpha = indicatorColor.alpha * selection.coerceIn(0f, 1f)
                                ),
                                cornerRadius = CornerRadius(size.height / 2)
                            )
                        }
                    }
                    .selectable(
                        selected = selected,
                        interactionSource = source,
                        indication = null,
                        role = Role.Tab,
                        onClick = { onTabSelected(tab) }
                    )
                    .semantics { contentDescription = label }
                    .heightIn(min = TabMinHeight),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(TabIconSize)
                )
                // La barra tiene ancho fijo por pestaña: más de 1.3× recortaría "Progreso"
                LimitFontScale(max = LabelMaxFontScale) {
                    Text(
                        text = label,
                        style = SegundoAireTheme.typography.labelSm,
                        color = tint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        // La pestaña ya se anuncia con su contentDescription
                        modifier = Modifier.clearAndSetSemantics {}
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavBarSample() {
    Box(
        Modifier
            .background(SegundoAireTheme.colors.background)
            .padding(top = SegundoAireTheme.spacing.space8)
    ) {
        BottomNavBar(selectedTab = MainTab.BLOCKS, onTabSelected = {})
    }
}

@Preview(name = "BottomNavBar · claro")
@Composable
private fun BottomNavBarLightPreview() {
    SegundoAireTheme(darkTheme = false) { BottomNavBarSample() }
}

@Preview(name = "BottomNavBar · oscuro")
@Composable
private fun BottomNavBarDarkPreview() {
    SegundoAireTheme(darkTheme = true) { BottomNavBarSample() }
}
