package com.segundoaire.presentation.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.segundoaire.presentation.designsystem.components.LimitFontScale
import com.segundoaire.presentation.designsystem.components.focusRing
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass

// Medidas de la referencia: barra de 68 dp con radio 32 e indicador de 64 × 32 a 6 dp del borde
private val BarHeight = 68.dp
private val BarRadius = 32.dp
private val IndicatorWidth = 64.dp
private val IndicatorHeight = 32.dp
private val IndicatorTop = 6.dp
private val TabIconSize = 22.dp
private val LabelGap = 2.dp
private const val LabelMaxFontScale = 1.3f
private const val ColorFadeMillis = 250

/**
 * Barra inferior flotante: vidrio `Level3` de 68 dp con radio 32, a 16 dp de los lados y
 * 20 dp del borde inferior. Un indicador de 64 × 32 dp en `primary` se desliza con
 * `Motion.navIndicator` hasta la pestaña activa, cuyo ícono pasa a `onPrimary` y su
 * etiqueta a peso 700. Con reducir movimiento el indicador no se desplaza: se funde en
 * la pestaña nueva. Cada pestaña lleva `Role.Tab` y su etiqueta como `contentDescription`.
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
    val shape = RoundedCornerShape(BarRadius)
    val reduced = motion.reduced
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val tabs = MainTab.entries
    val position by animateFloatAsState(
        targetValue = selectedTab.ordinal.toFloat(),
        animationSpec = motion.navIndicator(),
        label = "bottomNavIndicator"
    )

    Row(
        modifier = modifier
            .padding(start = spacing.space4, end = spacing.space4, bottom = spacing.space5)
            .fillMaxWidth()
            .height(BarHeight)
            .glass(GlassLevel.Level3, shape)
            .drawBehind {
                if (reduced) return@drawBehind
                val tabWidth = size.width / tabs.size
                val center = (position + 0.5f) * tabWidth
                val x = if (rtl) size.width - center else center
                val width = IndicatorWidth.toPx()
                val height = IndicatorHeight.toPx()
                drawRoundRect(
                    color = colors.primary,
                    topLeft = Offset(x - width / 2f, IndicatorTop.toPx()),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(height / 2f)
                )
            }
            .selectableGroup()
    ) {
        tabs.forEach { tab ->
            val selected = tab == selectedTab
            val label = stringResource(tab.labelRes)
            val source = remember { MutableInteractionSource() }
            val tint by animateColorAsState(
                targetValue = if (selected) colors.onPrimary else colors.onBackground,
                animationSpec = motion.fade(ColorFadeMillis),
                label = "bottomNavTint"
            )
            val fill by animateFloatAsState(
                targetValue = if (selected) 1f else 0f,
                animationSpec = motion.fade(ColorFadeMillis),
                label = "bottomNavFill"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .focusRing(source, shape)
                    .selectable(
                        selected = selected,
                        interactionSource = source,
                        indication = null,
                        role = Role.Tab,
                        onClick = { onTabSelected(tab) }
                    )
                    .semantics { contentDescription = label }
                    .padding(top = IndicatorTop),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(IndicatorWidth, IndicatorHeight)
                        .drawBehind {
                            // Sin desplazamiento, cada pestaña funde su propio indicador
                            if (reduced && fill > 0f) {
                                drawRoundRect(
                                    color = colors.primary.copy(alpha = fill),
                                    cornerRadius = CornerRadius(size.height / 2f)
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(TabIconSize)
                    )
                }
                Spacer(Modifier.height(LabelGap))
                // La barra tiene ancho fijo por pestaña: más de 1.3× recortaría "Progreso"
                LimitFontScale(max = LabelMaxFontScale) {
                    Text(
                        text = label,
                        style = SegundoAireTheme.typography.labelSm.copy(
                            fontWeight = if (selected) FontWeight.W700 else FontWeight.W500,
                            letterSpacing = SegundoAireTheme.typography.labelMd.letterSpacing
                        ),
                        color = colors.onBackground,
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
