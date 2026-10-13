package com.segundoaire.presentation.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.presentation.designsystem.theme.GlassBackdrop
import com.segundoaire.presentation.designsystem.theme.LocalSegundoAireMotion
import com.segundoaire.presentation.designsystem.theme.SegundoAireMotion
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glassBackdrop

/**
 * Catálogo de componentes del design system, solo en builds debug. El tema y
 * reducir movimiento se cambian aquí sin tocar los ajustes del sistema.
 */
@Composable
fun ComponentCatalogScreen() {
    val systemDark = isSystemInDarkTheme()
    var darkTheme by rememberSaveable { mutableStateOf(systemDark) }
    var reduceMotion by rememberSaveable { mutableStateOf(false) }

    SegundoAireTheme(darkTheme = darkTheme) {
        // Si el sistema ya reduce el movimiento, el switch no puede reactivarlo
        val systemReduced = SegundoAireTheme.motion.reduced
        val motion = remember(systemReduced, reduceMotion) {
            SegundoAireMotion(reduced = systemReduced || reduceMotion)
        }
        CompositionLocalProvider(LocalSegundoAireMotion provides motion) {
            CatalogContent(
                darkTheme = darkTheme,
                onDarkThemeChange = { darkTheme = it },
                reduceMotion = motion.reduced,
                reduceMotionEnabled = !systemReduced,
                onReduceMotionChange = { reduceMotion = it }
            )
        }
    }
}

@Composable
private fun CatalogContent(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    reduceMotion: Boolean,
    reduceMotionEnabled: Boolean,
    onReduceMotionChange: (Boolean) -> Unit
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing

    GlassBackdrop {
        Box(Modifier.fillMaxSize()) {
            CatalogBackground()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .safeDrawingPadding()
                    .padding(horizontal = spacing.space4, vertical = spacing.space6),
                verticalArrangement = Arrangement.spacedBy(spacing.space8)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    Text(
                        text = stringResource(R.string.catalog_title),
                        style = SegundoAireTheme.typography.headlineMd,
                        color = colors.onBackground,
                        modifier = Modifier.semantics { heading() }
                    )
                    CatalogSwitch(
                        label = stringResource(R.string.catalog_dark_theme),
                        checked = darkTheme,
                        onCheckedChange = onDarkThemeChange
                    )
                    CatalogSwitch(
                        label = stringResource(R.string.catalog_reduce_motion),
                        checked = reduceMotion,
                        enabled = reduceMotionEnabled,
                        onCheckedChange = onReduceMotionChange
                    )
                }
                CatalogSections()
            }
        }
    }
}

// Manchas de color detrás del contenido: sin ellas el blur del vidrio no se aprecia.
@Composable
private fun CatalogBackground() {
    val colors = SegundoAireTheme.colors
    Box(
        Modifier
            .fillMaxSize()
            .glassBackdrop()
            .background(colors.background)
            .drawBehind {
                drawCircle(
                    color = colors.primaryAlt,
                    radius = size.width * 0.45f,
                    center = Offset(size.width * 0.9f, size.height * 0.3f)
                )
                drawCircle(
                    color = colors.glow,
                    radius = size.width * 0.3f,
                    center = Offset(size.width * 0.1f, size.height * 0.7f)
                )
            }
    )
}

@Composable
private fun CatalogSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = SegundoAireTheme.typography.bodyLg,
            color = SegundoAireTheme.colors.onBackground,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

/** Una sección del catálogo: eyebrow con el nombre del componente y sus variantes debajo. */
@Composable
fun CatalogSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = SegundoAireTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
        Text(
            text = title.uppercase(),
            style = SegundoAireTheme.typography.labelSmCaps,
            color = SegundoAireTheme.colors.onBackgroundMuted,
            modifier = Modifier.semantics { heading() }
        )
        content()
    }
}

@Preview(name = "Catálogo · claro")
@Composable
private fun ComponentCatalogLightPreview() {
    SegundoAireTheme(darkTheme = false) {
        CatalogContent(
            darkTheme = false,
            onDarkThemeChange = {},
            reduceMotion = false,
            reduceMotionEnabled = true,
            onReduceMotionChange = {}
        )
    }
}

@Preview(name = "Catálogo · oscuro")
@Composable
private fun ComponentCatalogDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        CatalogContent(
            darkTheme = true,
            onDarkThemeChange = {},
            reduceMotion = false,
            reduceMotionEnabled = true,
            onReduceMotionChange = {}
        )
    }
}
