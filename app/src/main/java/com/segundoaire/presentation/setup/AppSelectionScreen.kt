package com.segundoaire.presentation.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.domain.model.InstalledApp
import com.segundoaire.presentation.designsystem.components.AppIcon
import com.segundoaire.presentation.designsystem.components.AppToggleRow
import com.segundoaire.presentation.designsystem.components.EmptyState
import com.segundoaire.presentation.designsystem.components.GlassTextField
import com.segundoaire.presentation.designsystem.components.QuestionScaffold
import com.segundoaire.presentation.designsystem.components.Skeleton
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme

private val ErrorIconSize = 20.dp
private val SkeletonRowHeight = 72.dp
private const val SkeletonRows = 5

/**
 * Selección de las apps a vigilar: búsqueda, contador, la sección "Sugeridas"
 * y el resto. Mientras se busca, todo se junta en una sola lista de resultados.
 *
 * [loadIcon] se pide solo cuando la fila entra en pantalla, así que una lista
 * larga no decodifica cientos de íconos de golpe.
 */
@Composable
fun AppSelectionScreen(
    state: AppSelectionUiState,
    onQueryChange: (String) -> Unit,
    onToggle: (packageName: String, selected: Boolean) -> Unit,
    onConfirm: () -> Unit,
    loadIcon: suspend (packageName: String) -> ImageBitmap?,
    modifier: Modifier = Modifier
) {
    val spacing = SegundoAireTheme.spacing
    val focusManager = LocalFocusManager.current

    QuestionScaffold(
        primaryLabel = stringResource(
            if (state.isSaving) R.string.setup_apps_saving else R.string.setup_apps_confirm
        ),
        onPrimary = onConfirm,
        modifier = modifier,
        primaryEnabled = state.canConfirm,
        title = stringResource(R.string.setup_apps_title),
        subtitle = stringResource(R.string.setup_apps_subtitle),
        scrollContent = false
    ) {
        GlassTextField(
            value = state.query,
            onValueChange = onQueryChange,
            label = stringResource(R.string.setup_apps_search_label),
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(R.string.setup_apps_search_placeholder),
            enabled = !state.isSaving && !state.isCompleted,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )

        Text(
            text = pluralStringResource(
                R.plurals.setup_apps_selected_count,
                state.selectedCount,
                state.selectedCount
            ),
            style = SegundoAireTheme.typography.labelMd,
            color = SegundoAireTheme.colors.onBackgroundMuted,
            modifier = Modifier
                .padding(top = spacing.space4, bottom = spacing.space2)
                .semantics { liveRegion = LiveRegionMode.Polite }
        )

        if (state.saveFailed) {
            SaveError(Modifier.padding(bottom = spacing.space2))
        }

        when {
            state.isLoading -> LoadingRows(Modifier.weight(1f))
            !state.hasInstalledApps -> CenteredEmptyState(
                title = stringResource(R.string.setup_apps_empty_title),
                message = stringResource(R.string.setup_apps_empty_message)
            )
            !state.hasResults -> CenteredEmptyState(
                title = stringResource(R.string.setup_apps_no_results_title),
                message = stringResource(R.string.setup_apps_no_results_message, state.query.trim())
            )
            else -> AppList(
                state = state,
                onToggle = onToggle,
                loadIcon = loadIcon,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AppList(
    state: AppSelectionUiState,
    onToggle: (String, Boolean) -> Unit,
    loadIcon: suspend (String) -> ImageBitmap?,
    modifier: Modifier = Modifier
) {
    val spacing = SegundoAireTheme.spacing
    val enabled = !state.isSaving && !state.isCompleted

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = spacing.space2),
        verticalArrangement = Arrangement.spacedBy(spacing.space2)
    ) {
        if (state.suggested.isNotEmpty()) {
            item(key = "header-suggested") { SectionHeader(stringResource(R.string.setup_apps_suggested)) }
            items(state.suggested, key = { "suggested-${it.packageName}" }) { app ->
                AppRow(app, state.selectedPackages, enabled, onToggle, loadIcon)
            }
        }
        if (state.apps.isNotEmpty()) {
            item(key = "header-apps") {
                SectionHeader(
                    stringResource(
                        if (state.isSearching) R.string.setup_apps_results else R.string.setup_apps_all
                    )
                )
            }
            items(state.apps, key = { "app-${it.packageName}" }) { app ->
                AppRow(app, state.selectedPackages, enabled, onToggle, loadIcon)
            }
        }
    }
}

@Composable
private fun AppRow(
    app: InstalledApp,
    selectedPackages: Set<String>,
    enabled: Boolean,
    onToggle: (String, Boolean) -> Unit,
    loadIcon: suspend (String) -> ImageBitmap?
) {
    AppToggleRow(
        name = app.appName,
        checked = app.packageName in selectedPackages,
        onCheckedChange = { onToggle(app.packageName, it) },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        AppIcon(packageName = app.packageName, loadIcon = loadIcon)
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = SegundoAireTheme.typography.titleMd,
        color = SegundoAireTheme.colors.onBackground,
        modifier = Modifier
            .padding(top = SegundoAireTheme.spacing.space4, bottom = SegundoAireTheme.spacing.space1)
            .semantics { heading() }
    )
}

@Composable
private fun SaveError(modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    Row(
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(SegundoAireTheme.spacing.space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = SegundoAireIcons.Error,
            contentDescription = null,
            tint = colors.error,
            modifier = Modifier.size(ErrorIconSize)
        )
        Text(
            text = stringResource(R.string.setup_apps_save_error),
            style = SegundoAireTheme.typography.bodyMd,
            color = colors.onBackground
        )
    }
}

@Composable
private fun ColumnScope.CenteredEmptyState(title: String, message: String) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        EmptyState(title = title, message = message)
    }
}

@Composable
private fun LoadingRows(modifier: Modifier = Modifier) {
    val spacing = SegundoAireTheme.spacing
    Column(
        modifier = modifier.padding(vertical = spacing.space2),
        verticalArrangement = Arrangement.spacedBy(spacing.space2)
    ) {
        repeat(SkeletonRows) {
            Skeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SkeletonRowHeight),
                shape = SegundoAireTheme.shapes.radiusMd
            )
        }
    }
}

private val PreviewApps = listOf(
    InstalledApp("com.example.video", "Videos"),
    InstalledApp("com.example.social", "Social"),
    InstalledApp("com.example.notes", "Notas"),
    InstalledApp("com.example.maps", "Mapas")
)

private val PreviewState = AppSelectionUiState(
    isLoading = false,
    hasInstalledApps = true,
    suggested = PreviewApps.take(2),
    apps = PreviewApps.drop(2),
    selectedPackages = setOf("com.example.social")
)

@Preview(name = "Selección de apps · claro", heightDp = 800)
@Composable
private fun AppSelectionScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) {
        AppSelectionScreen(
            state = PreviewState,
            onQueryChange = {},
            onToggle = { _, _ -> },
            onConfirm = {},
            loadIcon = { null }
        )
    }
}

@Preview(name = "Selección de apps · oscuro", heightDp = 800)
@Composable
private fun AppSelectionScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        AppSelectionScreen(
            state = PreviewState.copy(query = "m", suggested = emptyList(), apps = PreviewApps.takeLast(2), saveFailed = true),
            onQueryChange = {},
            onToggle = { _, _ -> },
            onConfirm = {},
            loadIcon = { null }
        )
    }
}
