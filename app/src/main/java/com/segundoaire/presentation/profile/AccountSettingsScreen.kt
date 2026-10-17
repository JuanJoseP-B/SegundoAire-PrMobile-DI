package com.segundoaire.presentation.profile

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.domain.model.AppLanguage
import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.model.ThemeMode
import com.segundoaire.presentation.designsystem.components.AppToggleRow
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.components.SegmentedSelector
import com.segundoaire.presentation.designsystem.components.TextButton
import com.segundoaire.presentation.designsystem.components.focusRing
import com.segundoaire.presentation.designsystem.icons.SegundoAireIcons
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.designsystem.theme.glass
import com.segundoaire.presentation.onboarding.OnboardingEyebrow
/** Lo que Ajustes le pide al exterior, agrupado para no alargar la firma de la pantalla. */
@Immutable
class SettingsActions(
    val onLanguageSelected: (AppLanguage) -> Unit = {},
    val onThemeModeSelected: (ThemeMode) -> Unit = {},
    val onOpenTutorial: () -> Unit = {},
    val onDemoModeChange: (Boolean) -> Unit = {},
    val onResetConfirmed: () -> Unit = {}
)


/**
 * "Cuenta" del Perfil: el modo de cuenta (simulado), idioma, tema, tutorial, modo demo y
 * reiniciar. Cada ajuste se aplica al momento. Sin estado propio: [AccountSettingsRoute]
 * lo conecta.
 */
@Composable
fun AccountSettingsScreen(
    settings: SettingsUiState,
    actions: SettingsActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = SegundoAireTheme.spacing
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SegundoAireTheme.colors.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = spacing.space5, end = spacing.space5, bottom = spacing.space6),
        verticalArrangement = Arrangement.spacedBy(spacing.space4)
    ) {
        AccountHeader(onBack = onBack)
        AccountCard(authMode = settings.authMode, email = settings.email)
        SettingsSection(settings = settings, actions = actions)
    }
}

@Composable
private fun AccountHeader(onBack: () -> Unit) {
    val colors = SegundoAireTheme.colors
    val source = remember { MutableInteractionSource() }
    val backLabel = stringResource(R.string.onboarding_back)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(48.dp)
                .focusRing(source, CircleShape)
                .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onBack)
                .semantics { contentDescription = backLabel },
            contentAlignment = Alignment.Center
        ) {
            Icon(SegundoAireIcons.ArrowBack, contentDescription = null, tint = colors.onBackground)
        }
        Spacer(Modifier.width(SegundoAireTheme.spacing.space2))
        Text(
            text = stringResource(R.string.account_title),
            style = SegundoAireTheme.typography.serif(size = 30, lineHeight = 36),
            color = colors.onBackground,
            modifier = Modifier.semantics { heading() }
        )
    }
}

@Composable
private fun AccountCard(authMode: AuthMode?, email: String, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        level = GlassLevel.Level2
    ) {
        OnboardingEyebrow(stringResource(R.string.profile_account_title))
        Text(
            text = stringResource((authMode ?: AuthMode.GUEST).accountLabelRes),
            style = typography.titleMd,
            color = colors.onBackground,
            modifier = Modifier.padding(top = SegundoAireTheme.spacing.space2)
        )
        if (email.isNotBlank()) {
            Text(text = email, style = typography.bodyMd, color = colors.onBackground)
        }
        Text(
            text = stringResource(R.string.profile_account_simulated),
            style = typography.bodyMd,
            color = colors.onBackground,
            modifier = Modifier.padding(top = SegundoAireTheme.spacing.space2)
        )
    }
}

@Composable
private fun SettingsSection(
    settings: SettingsUiState,
    actions: SettingsActions,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val spacing = SegundoAireTheme.spacing
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.space4)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = SegundoAireTheme.typography.titleLg,
            color = colors.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        LanguageCard(language = settings.language, onLanguageSelected = actions.onLanguageSelected)
        ThemeCard(themeMode = settings.themeMode, onThemeModeSelected = actions.onThemeModeSelected)
        TutorialEntryCard(onClick = actions.onOpenTutorial)
        DemoModeRow(enabled = settings.isDemoMode, onCheckedChange = actions.onDemoModeChange)
        ResetCard(
            isResetting = settings.isResetting,
            resetFailed = settings.resetFailed,
            onResetConfirmed = actions.onResetConfirmed
        )
    }
}

/** Interruptor del modo demo: Progreso lee datos de ejemplo, sin tocar el historial real. */
@Composable
private fun DemoModeRow(
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = SegundoAireTheme.spacing
    AppToggleRow(
        name = stringResource(R.string.settings_demo_title),
        checked = enabled,
        onCheckedChange = onCheckedChange,
        modifier = modifier.fillMaxWidth(),
        supportingText = stringResource(R.string.settings_demo_message)
    ) {
        Box(
            modifier = Modifier
                .size(spacing.space12)
                .glass(GlassLevel.Level1, SegundoAireTheme.shapes.radiusSm),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = SegundoAireIcons.Progress,
                contentDescription = null,
                tint = SegundoAireTheme.colors.primary
            )
        }
    }
}

/** "Reiniciar app": pide confirmación antes de borrar nada. */
@Composable
private fun ResetCard(
    isResetting: Boolean,
    resetFailed: Boolean,
    onResetConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val spacing = SegundoAireTheme.spacing
    var showDialog by rememberSaveable { mutableStateOf(false) }

    GlassCard(modifier = modifier.fillMaxWidth(), level = GlassLevel.Level2) {
        Text(
            text = stringResource(R.string.settings_reset_title),
            style = typography.titleMd,
            color = colors.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(R.string.settings_reset_message),
            style = typography.bodyMd,
            color = colors.onBackground
        )
        if (isResetting) {
            Text(
                text = stringResource(R.string.settings_reset_working),
                style = typography.bodyMd,
                color = colors.onBackground,
                modifier = Modifier
                    .padding(top = spacing.space3)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
        } else {
            if (resetFailed) {
                Text(
                    text = stringResource(R.string.settings_reset_failed),
                    style = typography.bodyMd,
                    color = colors.error,
                    modifier = Modifier
                        .padding(top = spacing.space3)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
            TextButton(
                text = stringResource(R.string.settings_reset_action),
                onClick = { showDialog = true },
                color = colors.error
            )
        }
    }

    if (showDialog) {
        ResetConfirmDialog(
            onConfirm = {
                showDialog = false
                onResetConfirmed()
            },
            onDismiss = { showDialog = false }
        )
    }
}

/** Abre el tutorial: solo se llega a él desde aquí, nunca aparece solo. */
@Composable
private fun TutorialEntryCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SegundoAireTheme.colors
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        level = GlassLevel.Level2,
        onClick = onClick
    ) {
        Text(
            text = stringResource(R.string.settings_tutorial_title),
            style = SegundoAireTheme.typography.titleMd,
            color = colors.onBackground
        )
        Text(
            text = stringResource(R.string.settings_tutorial_message),
            style = SegundoAireTheme.typography.bodyMd,
            color = colors.onBackground
        )
    }
}

@Composable
private fun ThemeCard(
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = ThemeMode.entries
    GlassCard(modifier = modifier.fillMaxWidth(), level = GlassLevel.Level2) {
        Text(
            text = stringResource(R.string.settings_theme_title),
            style = SegundoAireTheme.typography.titleMd,
            color = SegundoAireTheme.colors.onBackground,
            modifier = Modifier
                .padding(bottom = SegundoAireTheme.spacing.space3)
                .semantics { heading() }
        )
        SegmentedSelector(
            options = options.map { stringResource(it.labelRes) },
            selectedIndex = options.indexOf(themeMode),
            onSelected = { onThemeModeSelected(options[it]) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LanguageCard(
    language: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = AppLanguage.entries
    GlassCard(modifier = modifier.fillMaxWidth(), level = GlassLevel.Level2) {
        Text(
            text = stringResource(R.string.settings_language_title),
            style = SegundoAireTheme.typography.titleMd,
            color = SegundoAireTheme.colors.onBackground,
            modifier = Modifier
                .padding(bottom = SegundoAireTheme.spacing.space3)
                .semantics { heading() }
        )
        SegmentedSelector(
            options = options.map { stringResource(it.labelRes) },
            selectedIndex = options.indexOf(language),
            onSelected = { onLanguageSelected(options[it]) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// El dominio no conoce los recursos: aquí se asocia cada modo de cuenta a su cadena.
@get:StringRes
private val AuthMode.accountLabelRes: Int
    get() = when (this) {
        AuthMode.GOOGLE -> R.string.profile_account_google
        AuthMode.APPLE -> R.string.profile_account_apple
        AuthMode.EMAIL -> R.string.profile_account_email
        AuthMode.GUEST -> R.string.profile_account_guest
    }

// Los nombres de idioma van en su propio idioma; "Sistema" sí se traduce.
@get:StringRes
private val AppLanguage.labelRes: Int
    get() = when (this) {
        AppLanguage.SYSTEM -> R.string.settings_language_system
        AppLanguage.SPANISH -> R.string.settings_language_spanish
        AppLanguage.ENGLISH -> R.string.settings_language_english
    }

@get:StringRes
private val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
    }

@Composable
private fun AccountSettingsSample(settings: SettingsUiState) {
    AccountSettingsScreen(settings = settings, actions = SettingsActions(), onBack = {})
}

@Preview(name = "Cuenta · claro", heightDp = 1100)
@Composable
private fun AccountSettingsLightPreview() {
    SegundoAireTheme(darkTheme = false) { AccountSettingsSample(SettingsUiState(authMode = AuthMode.GUEST)) }
}

@Preview(name = "Cuenta · oscuro", heightDp = 1100)
@Composable
private fun AccountSettingsDarkPreview() {
    SegundoAireTheme(darkTheme = true) {
        AccountSettingsSample(SettingsUiState(language = AppLanguage.SPANISH, themeMode = ThemeMode.DARK, authMode = AuthMode.EMAIL))
    }
}
