package com.segundoaire.presentation.panel

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.domain.model.BlockedApp
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.GreetingPeriod
import com.segundoaire.presentation.designsystem.components.AnimatedCounter
import com.segundoaire.presentation.designsystem.components.AppIcon
import com.segundoaire.presentation.designsystem.components.AppToggleRow
import com.segundoaire.presentation.designsystem.components.EmptyState
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.components.PermissionBanner
import com.segundoaire.presentation.designsystem.components.RevealText
import com.segundoaire.presentation.designsystem.components.Skeleton
import com.segundoaire.presentation.designsystem.components.TextButton
import com.segundoaire.presentation.designsystem.components.staggerIn
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.OnboardingEyebrow
import com.segundoaire.presentation.onboarding.labelRes

private val SkeletonRowHeight = 72.dp
private const val SkeletonRows = 3

/**
 * Panel: saludo con el objetivo, dato de impacto, mensaje de la IA simulada y
 * la lista de apps vigiladas. Sin estado propio: [PanelRoute] lo conecta.
 *
 * La lista anima su entrada solo la primera vez que cada app aparece en la
 * sesión (`animatedPackages`); después solo se anima el alta, la baja y el
 * reordenamiento de filas con `animateItem`.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PanelScreen(
    state: PanelUiState,
    onToggleApp: (packageName: String, isBlocked: Boolean) -> Unit,
    onEditApps: () -> Unit,
    onFixPermission: () -> Unit,
    onNewMessage: () -> Unit,
    onAppsAnimated: (List<String>) -> Unit,
    loadIcon: suspend (packageName: String) -> ImageBitmap?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    onTitleLongClick: (() -> Unit)? = null
) {
    val spacing = SegundoAireTheme.spacing
    val motion = SegundoAireTheme.motion
    val direction = LocalLayoutDirection.current

    // Las apps que aún no se animaron; se marcan al componerlas por primera vez
    val pending = remember(state.apps, state.animatedPackages) {
        state.apps.map { it.packageName }.filter { it !in state.animatedPackages }
    }
    LaunchedEffect(pending) {
        if (pending.isNotEmpty()) onAppsAnimated(pending)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.space5 + contentPadding.calculateStartPadding(direction),
            top = spacing.space6 + contentPadding.calculateTopPadding(),
            end = spacing.space5 + contentPadding.calculateEndPadding(direction),
            bottom = spacing.space6 + contentPadding.calculateBottomPadding()
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.space4)
    ) {
        item(key = "header") {
            PanelHeader(state = state, onLongClick = onTitleLongClick)
        }
        state.missingPermission?.let { missing ->
            item(key = "permission") {
                PermissionBanner(
                    title = stringResource(R.string.banner_title),
                    message = stringResource(
                        when (missing) {
                            MissingPermission.ACCESSIBILITY -> R.string.banner_accessibility_disabled
                            MissingPermission.OVERLAY -> R.string.banner_overlay_disabled
                        }
                    ),
                    actionLabel = stringResource(R.string.banner_action),
                    onAction = onFixPermission,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item(key = "impact") {
            ImpactCard(watchedApps = state.watchedAppsCount, modifier = Modifier.fillMaxWidth())
        }
        if (state.message != null || state.isThinking) {
            item(key = "message") {
                MessageCard(
                    message = state.message,
                    isThinking = state.isThinking,
                    onNewMessage = onNewMessage,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item(key = "apps-header") {
            AppsHeader(showEdit = state.hasApps, onEditApps = onEditApps)
        }
        when {
            state.isLoading -> items(SkeletonRows, key = { "skeleton-$it" }) {
                Skeleton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SkeletonRowHeight),
                    shape = SegundoAireTheme.shapes.radiusMd
                )
            }
            !state.hasApps -> item(key = "empty") {
                EmptyState(
                    title = stringResource(R.string.panel_empty_title),
                    message = stringResource(R.string.panel_empty_message),
                    modifier = Modifier.fillMaxWidth(),
                    icon = SegundoAireIcons.Device,
                    actionLabel = stringResource(R.string.panel_empty_action),
                    onAction = onEditApps
                )
            }
            else -> items(state.apps, key = { it.packageName }) { app ->
                // Se decide una sola vez: cambiarlo a mitad cortaría la animación de entrada
                val staggerIndex = remember { pending.indexOf(app.packageName) }
                AppToggleRow(
                    name = app.appName,
                    checked = app.isBlocked,
                    onCheckedChange = { onToggleApp(app.packageName, it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(
                            fadeInSpec = motion.nav(),
                            placementSpec = motion.nav(),
                            fadeOutSpec = motion.nav()
                        )
                        .staggerIn(animate = staggerIndex >= 0, index = staggerIndex),
                    supportingText = stringResource(
                        if (app.isBlocked) R.string.panel_app_watching else R.string.panel_app_off
                    )
                ) {
                    AppIcon(packageName = app.packageName, loadIcon = loadIcon)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PanelHeader(
    state: PanelUiState,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val typography = SegundoAireTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        interactionSource = null,
                        indication = null,
                        onLongClick = onLongClick,
                        onClick = {}
                    )
                } else {
                    Modifier
                }
            ),
        verticalArrangement = Arrangement.spacedBy(spacing.space1)
    ) {
        Text(
            text = greetingText(state.greeting, state.name.trim()),
            style = typography.headlineMd,
            color = colors.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        state.mainGoal?.let { goal ->
            Text(
                text = stringResource(R.string.panel_goal_eyebrow).toUpperCase(Locale.current),
                style = typography.labelSmCaps,
                color = colors.onBackgroundMuted,
                modifier = Modifier.padding(top = spacing.space2)
            )
            Text(
                text = stringResource(goal.labelRes),
                style = typography.bodyLg,
                color = colors.onBackgroundMuted
            )
        }
    }
}

@Composable
private fun greetingText(period: GreetingPeriod, name: String): String =
    if (name.isEmpty()) {
        stringResource(
            when (period) {
                GreetingPeriod.MORNING -> R.string.panel_greeting_morning_anonymous
                GreetingPeriod.AFTERNOON -> R.string.panel_greeting_afternoon_anonymous
                GreetingPeriod.EVENING -> R.string.panel_greeting_evening_anonymous
            }
        )
    } else {
        stringResource(
            when (period) {
                GreetingPeriod.MORNING -> R.string.panel_greeting_morning
                GreetingPeriod.AFTERNOON -> R.string.panel_greeting_afternoon
                GreetingPeriod.EVENING -> R.string.panel_greeting_evening
            },
            name
        )
    }

/** Dato de impacto del día. Hoy cuenta las apps vigiladas; la Fase 15 pondrá aquí los minutos. */
@Composable
private fun ImpactCard(watchedApps: Int, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    GlassCard(modifier = modifier, level = GlassLevel.Level2) {
        OnboardingEyebrow(stringResource(R.string.panel_impact_eyebrow))
        AnimatedCounter(
            value = watchedApps,
            style = SegundoAireTheme.typography.displayImpact,
            color = colors.onBackground
        )
        Text(
            text = pluralStringResource(R.plurals.panel_impact_watched, watchedApps),
            style = SegundoAireTheme.typography.bodyMd,
            color = colors.onBackground
        )
    }
}

/**
 * Tarjeta de la IA simulada. Mientras piensa, el borde se enciende con `glow`
 * (`Motion.calm`); el mensaje nuevo se revela carácter a carácter.
 */
@Composable
private fun MessageCard(
    message: String?,
    isThinking: Boolean,
    onNewMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    val glowAlpha by animateFloatAsState(
        targetValue = if (isThinking) 1f else 0f,
        animationSpec = SegundoAireTheme.motion.calm(),
        label = "messageGlow"
    )

    GlassCard(
        modifier = modifier.border(
            width = 2.dp,
            color = colors.glow.copy(alpha = colors.glow.alpha * glowAlpha.coerceIn(0f, 1f)),
            shape = SegundoAireTheme.shapes.radiusLg
        ),
        level = GlassLevel.Level2
    ) {
        OnboardingEyebrow(stringResource(R.string.panel_message_eyebrow))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.space2)
        ) {
            if (message != null) {
                RevealText(text = message, color = colors.onBackground)
            } else {
                Text(
                    text = stringResource(R.string.panel_message_thinking),
                    style = SegundoAireTheme.typography.bodyLg,
                    color = colors.onBackground
                )
            }
        }
        TextButton(
            text = stringResource(R.string.panel_message_another),
            onClick = onNewMessage,
            modifier = Modifier.align(Alignment.End),
            enabled = !isThinking
        )
    }
}

@Composable
private fun AppsHeader(showEdit: Boolean, onEditApps: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.panel_apps_title),
            style = SegundoAireTheme.typography.titleLg,
            color = SegundoAireTheme.colors.onBackground,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )
        if (showEdit) {
            TextButton(text = stringResource(R.string.panel_apps_edit), onClick = onEditApps)
        }
    }
}

private val PreviewApps = listOf(
    BlockedApp("com.example.video", "Videos"),
    BlockedApp("com.example.social", "Social"),
    BlockedApp("com.example.news", "Noticias", isBlocked = false)
)

private val PreviewState = PanelUiState(
    isLoading = false,
    greeting = GreetingPeriod.MORNING,
    name = "Ana",
    mainGoal = Goal.SLEEP,
    apps = PreviewApps,
    animatedPackages = PreviewApps.map { it.packageName }.toSet(),
    message = "Respira. Tú decides en qué pones tu atención hoy."
)

@Composable
private fun PanelScreenSample(state: PanelUiState) {
    PanelScreen(
        state = state,
        onToggleApp = { _, _ -> },
        onEditApps = {},
        onFixPermission = {},
        onNewMessage = {},
        onAppsAnimated = {},
        loadIcon = { null },
        modifier = Modifier.background(SegundoAireTheme.colors.background)
    )
}

@Preview(name = "Panel · claro", heightDp = 800)
@Composable
private fun PanelScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) { PanelScreenSample(PreviewState) }
}

@Preview(name = "Panel · oscuro", heightDp = 800)
@Composable
private fun PanelScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        PanelScreenSample(
            PreviewState.copy(isAccessibilityEnabled = false, isThinking = true)
        )
    }
}

@Preview(name = "Panel sin apps · claro", heightDp = 800)
@Composable
private fun PanelScreenEmptyLightPreview() {
    SegundoAireTheme(darkTheme = false) {
        PanelScreenSample(PreviewState.copy(apps = emptyList(), mainGoal = null, name = ""))
    }
}

@Preview(name = "Panel sin apps · oscuro", heightDp = 800)
@Composable
private fun PanelScreenEmptyDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        PanelScreenSample(PreviewState.copy(apps = emptyList(), message = null, isThinking = true))
    }
}
