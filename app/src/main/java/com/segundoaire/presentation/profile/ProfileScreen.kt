package com.segundoaire.presentation.profile

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segundoaire.R
import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.presentation.designsystem.components.ChoiceChipGroup
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.components.GlassTextField
import com.segundoaire.presentation.designsystem.components.PrimaryButton
import com.segundoaire.presentation.designsystem.components.Skeleton
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.NameError
import com.segundoaire.presentation.onboarding.OnboardingEyebrow
import com.segundoaire.presentation.onboarding.labelRes

private val SkeletonCardHeight = 128.dp

/**
 * Perfil: nombre y objetivos editables, y el modo de cuenta (simulado) en solo
 * lectura. Un único botón guarda ambos. Sin estado propio: [ProfileRoute] lo conecta.
 */
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onNameChange: (String) -> Unit,
    onGoalsChange: (List<Goal>) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val spacing = SegundoAireTheme.spacing
    val direction = LocalLayoutDirection.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = spacing.space5 + contentPadding.calculateStartPadding(direction),
                top = spacing.space6 + contentPadding.calculateTopPadding(),
                end = spacing.space5 + contentPadding.calculateEndPadding(direction),
                bottom = spacing.space6 + contentPadding.calculateBottomPadding()
            ),
        verticalArrangement = Arrangement.spacedBy(spacing.space6)
    ) {
        Text(
            text = stringResource(R.string.profile_title),
            style = SegundoAireTheme.typography.headlineMd,
            color = SegundoAireTheme.colors.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        if (state.isLoading) {
            repeat(3) {
                Skeleton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SkeletonCardHeight),
                    shape = SegundoAireTheme.shapes.radiusLg
                )
            }
        } else {
            NameCard(state = state, onNameChange = onNameChange)
            GoalsCard(goals = state.goals, onGoalsChange = onGoalsChange)
            AccountCard(authMode = state.authMode, email = state.email)
            SaveSection(state = state, onSave = onSave)
        }
    }
}

@Composable
private fun NameCard(
    state: ProfileUiState,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    GlassCard(modifier = modifier.fillMaxWidth(), level = GlassLevel.Level2) {
        GlassTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = stringResource(R.string.onboarding_name_label),
            modifier = Modifier.fillMaxWidth(),
            errorMessage = when (state.nameError) {
                NameError.INVALID_CHARACTERS -> stringResource(R.string.onboarding_name_error)
                null -> null
            },
            enabled = !state.isSaving,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
    }
}

@Composable
private fun GoalsCard(
    goals: List<Goal>,
    onGoalsChange: (List<Goal>) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    val typography = SegundoAireTheme.typography
    val options = Goal.entries
    GlassCard(modifier = modifier.fillMaxWidth(), level = GlassLevel.Level2) {
        Text(
            text = stringResource(R.string.profile_goals_title),
            style = typography.titleMd,
            color = colors.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(R.string.profile_goals_subtitle, UserProfile.MAX_GOALS),
            style = typography.bodyMd,
            color = colors.onBackground,
            modifier = Modifier.padding(bottom = SegundoAireTheme.spacing.space3)
        )
        ChoiceChipGroup(
            options = options.map { stringResource(it.labelRes) },
            // El conjunto conserva el orden de elección: el primero es el objetivo principal
            selected = goals.mapTo(linkedSetOf()) { it.ordinal },
            onSelectionChange = { selected -> onGoalsChange(selected.map { options[it] }) },
            modifier = Modifier.fillMaxWidth(),
            maxSelected = UserProfile.MAX_GOALS
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
private fun SaveSection(
    state: ProfileUiState,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SegundoAireTheme.colors
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SegundoAireTheme.spacing.space3)
    ) {
        PrimaryButton(
            text = stringResource(R.string.profile_save),
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.canSave
        )
        val feedback = when (state.feedback) {
            SaveFeedback.SAVED -> stringResource(R.string.profile_saved) to colors.onBackground
            SaveFeedback.FAILED -> stringResource(R.string.profile_save_failed) to colors.error
            null -> null
        }
        if (feedback != null) {
            Text(
                text = feedback.first,
                style = SegundoAireTheme.typography.bodyMd,
                color = feedback.second,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
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

@Composable
private fun ProfileScreenSample(state: ProfileUiState) {
    ProfileScreen(
        state = state,
        onNameChange = {},
        onGoalsChange = {},
        onSave = {},
        modifier = Modifier.background(SegundoAireTheme.colors.background)
    )
}

@Composable
private fun previewState(
    feedback: SaveFeedback? = null,
    nameError: NameError? = null
) = ProfileUiState(
    isLoading = false,
    name = stringResource(R.string.onboarding_name_placeholder),
    nameError = nameError,
    goals = listOf(Goal.SLEEP, Goal.FOCUS),
    authMode = AuthMode.EMAIL,
    email = stringResource(R.string.auth_email_placeholder),
    hasChanges = true,
    feedback = feedback
)

@Preview(name = "Perfil · claro", heightDp = 800)
@Composable
private fun ProfileScreenLightPreview() {
    SegundoAireTheme(darkTheme = false) { ProfileScreenSample(previewState(SaveFeedback.SAVED)) }
}

@Preview(name = "Perfil · oscuro", heightDp = 800)
@Composable
private fun ProfileScreenDarkPreview() {
    SegundoAireTheme(darkTheme = true) { ProfileScreenSample(previewState(SaveFeedback.FAILED)) }
}

@Preview(name = "Perfil con error · claro", heightDp = 800)
@Composable
private fun ProfileScreenErrorLightPreview() {
    SegundoAireTheme(darkTheme = false) {
        ProfileScreenSample(previewState(nameError = NameError.INVALID_CHARACTERS))
    }
}

@Preview(name = "Perfil cargando · oscuro", heightDp = 800)
@Composable
private fun ProfileScreenLoadingDarkPreview() {
    SegundoAireTheme(darkTheme = true) { ProfileScreenSample(ProfileUiState()) }
}
