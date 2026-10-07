package com.segundoaire.presentation.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segundoaire.R
import com.segundoaire.domain.model.BlockedApp
import com.segundoaire.presentation.theme.SegundoAireTheme

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Al volver de Ajustes se vuelven a leer los permisos.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshPermissions() }

    DashboardContent(
        state = state,
        modifier = modifier,
        onFixPermissions = { openPermissionSettings(context, state) },
        onToggleApp = viewModel::onToggleApp
    )
}

@Composable
private fun DashboardContent(
    state: MainUiState,
    onFixPermissions: () -> Unit,
    onToggleApp: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (state.needsPermissions) {
                item {
                    PermissionBanner(state = state, onFix = onFixPermissions)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        label = stringResource(R.string.dashboard_focused_today),
                        value = stringResource(R.string.dashboard_minutes_format, state.focusedMinutesToday),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = stringResource(R.string.dashboard_watched_apps),
                        value = state.watchedAppsCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Text(
                    text = stringResource(R.string.dashboard_apps_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (state.blockedApps.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.dashboard_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(state.blockedApps, key = { it.packageName }) { app ->
                AppRow(app = app, onToggle = { onToggleApp(app.packageName, it) })
            }
        }
    }
}

@Composable
private fun PermissionBanner(state: MainUiState, onFix: () -> Unit) {
    val message = when {
        !state.isAccessibilityEnabled -> R.string.banner_accessibility_disabled
        else -> R.string.banner_overlay_disabled
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.banner_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onFix,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(stringResource(R.string.banner_action))
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = SegundoAireTheme.glass.surface,
        border = BorderStroke(1.dp, SegundoAireTheme.glass.border)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AppRow(app: BlockedApp, onToggle: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SegundoAireTheme.glass.surface,
        border = BorderStroke(1.dp, SegundoAireTheme.glass.border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Switch(checked = app.isBlocked, onCheckedChange = onToggle)
        }
    }
}

private fun openPermissionSettings(context: Context, state: MainUiState) {
    val intent = if (!state.isAccessibilityEnabled) {
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    } else {
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
    }
    context.startActivity(intent)
}
