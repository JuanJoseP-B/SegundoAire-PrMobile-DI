package com.segundoaire.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.segundoaire.R
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.presentation.designsystem.components.ChoiceChipGroup
import com.segundoaire.presentation.designsystem.components.GlassCard
import com.segundoaire.presentation.designsystem.components.GlassTextField
import com.segundoaire.presentation.designsystem.components.PrimaryButton
import com.segundoaire.presentation.designsystem.theme.GlassLevel
import com.segundoaire.presentation.designsystem.theme.SegundoAireTheme
import com.segundoaire.presentation.onboarding.NameError
import com.segundoaire.presentation.onboarding.labelRes

/**
 * Hoja "Editar" del Perfil: nombre y metas con la lógica de siempre (un solo botón guarda
 * ambos) sobre una hoja inferior. Al guardar sin errores se cierra.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditSheet(
    state: ProfileUiState,
    onNameChange: (String) -> Unit,
    onGoalsChange: (List<Goal>) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val spacing = SegundoAireTheme.spacing
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SegundoAireTheme.colors.background
    ) {
        ProfileEditContent(state, onNameChange, onGoalsChange, onSave, Modifier.padding(horizontal = spacing.space5))
    }
}

@Composable
private fun ProfileEditContent(
    state: ProfileUiState,
    onNameChange: (String) -> Unit,
    onGoalsChange: (List<Goal>) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = SegundoAireTheme.spacing
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = spacing.space6),
        verticalArrangement = Arrangement.spacedBy(spacing.space4)
    ) {
        Text(
            text = stringResource(R.string.profile_edit_title),
            style = SegundoAireTheme.typography.serif(size = 26, lineHeight = 32),
            color = SegundoAireTheme.colors.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        NameCard(state = state, onNameChange = onNameChange)
        GoalsCard(goals = state.goals, onGoalsChange = onGoalsChange)
        SaveSection(state = state, onSave = onSave)
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

@Composable
private fun ProfileEditSample(state: ProfileUiState) {
    ProfileEditContent(state, {}, {}, {}, Modifier.background(SegundoAireTheme.colors.background).padding(SegundoAireTheme.spacing.space5))
}

@Composable
private fun previewEditState(nameError: NameError? = null) = ProfileUiState(
    isLoading = false,
    name = stringResource(R.string.onboarding_name_placeholder),
    nameError = nameError,
    goals = listOf(Goal.SLEEP, Goal.FOCUS),
    hasChanges = true
)

@Preview(name = "Editar perfil · claro")
@Composable
private fun ProfileEditLightPreview() {
    SegundoAireTheme(darkTheme = false) { ProfileEditSample(previewEditState()) }
}

@Preview(name = "Editar perfil · oscuro, con error")
@Composable
private fun ProfileEditDarkPreview() {
    SegundoAireTheme(darkTheme = true) { ProfileEditSample(previewEditState(NameError.INVALID_CHARACTERS)) }
}
