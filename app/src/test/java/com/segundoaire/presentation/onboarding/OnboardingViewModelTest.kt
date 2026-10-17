package com.segundoaire.presentation.onboarding

import androidx.lifecycle.SavedStateHandle
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.RiskMoment
import com.segundoaire.domain.model.UsageRange
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.domain.usecase.CompleteOnboardingUseCase
import com.segundoaire.domain.usecase.SaveUserProfileUseCase
import com.segundoaire.domain.usecase.SuggestRigorUseCase
import com.segundoaire.testing.FakeUserPreferencesRepository
import com.segundoaire.testing.FakeUserProfileRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingViewModelTest {

    private val profiles = FakeUserProfileRepository()
    private val preferences = FakeUserPreferencesRepository()

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) = OnboardingViewModel(
        savedStateHandle = savedStateHandle,
        suggestRigor = SuggestRigorUseCase(),
        saveUserProfile = SaveUserProfileUseCase(profiles),
        completeOnboarding = CompleteOnboardingUseCase(preferences)
    )

    private fun OnboardingViewModel.answerEverything() {
        onNameChange("Ana")
        onGoalsChange(listOf(Goal.SLEEP))
        onUsageRangeChange(UsageRange.FROM_2_TO_4)
        onRiskMomentsChange(setOf(RiskMoment.BEFORE_SLEEP))
    }

    // Pasos

    @Test
    fun `el flujo arranca en la semilla`() {
        assertEquals(OnboardingStep.Seed, viewModel().uiState.value.step)
    }

    @Test
    fun `el paso se mantiene dentro de los catorce pasos`() {
        val viewModel = viewModel()

        viewModel.onStepChange(40)
        assertEquals(OnboardingStep.Done, viewModel.uiState.value.step)

        viewModel.onStepChange(-3)
        assertEquals(OnboardingStep.Seed, viewModel.uiState.value.step)
    }

    @Test
    fun `entrar por el login no hace retroceder a quien ya lo pasó`() {
        val viewModel = viewModel()
        viewModel.onStepChange(OnboardingStep.Done)

        viewModel.ensureStartStep(OnboardingStep.Login)

        assertEquals(OnboardingStep.Done, viewModel.uiState.value.step)
    }

    @Test
    fun `entrar por el login salta a ese paso`() {
        val viewModel = viewModel()

        viewModel.ensureStartStep(OnboardingStep.Login)

        assertEquals(OnboardingStep.Login, viewModel.uiState.value.step)
    }

    // Nombre

    @Test
    fun `sin nombre no se puede continuar y no hay error`() {
        val state = viewModel().uiState.value

        assertFalse(state.canContinue(OnboardingStep.Name))
        assertNull(state.nameError)
    }

    @Test
    fun `un nombre solo de espacios no alcanza`() {
        val viewModel = viewModel()

        viewModel.onNameChange("   ")

        assertFalse(viewModel.uiState.value.canContinue(OnboardingStep.Name))
    }

    @Test
    fun `acepta nombres con tildes, guion y apostrofo`() {
        val viewModel = viewModel()

        listOf("José", "Ana-María", "O'Connor", "María José").forEach { name ->
            viewModel.onNameChange(name)

            assertNull(name, viewModel.uiState.value.nameError)
            assertTrue(name, viewModel.uiState.value.canContinue(OnboardingStep.Name))
        }
    }

    @Test
    fun `un nombre con numeros o simbolos marca error`() {
        val viewModel = viewModel()

        viewModel.onNameChange("Ana99")

        assertEquals(NameError.INVALID_CHARACTERS, viewModel.uiState.value.nameError)
        assertFalse(viewModel.uiState.value.canContinue(OnboardingStep.Name))
    }

    @Test
    fun `el nombre se corta en el largo maximo`() {
        val viewModel = viewModel()

        viewModel.onNameChange("a".repeat(UserProfile.MAX_NAME_LENGTH + 10))

        assertEquals(UserProfile.MAX_NAME_LENGTH, viewModel.uiState.value.name.length)
    }

    // Objetivos, uso y momentos

    @Test
    fun `los objetivos se limitan a dos y conservan el orden`() {
        val viewModel = viewModel()

        viewModel.onGoalsChange(listOf(Goal.PRESENCE, Goal.FOCUS, Goal.SLEEP))

        assertEquals(listOf(Goal.PRESENCE, Goal.FOCUS), viewModel.uiState.value.goals)
    }

    @Test
    fun `hace falta al menos un objetivo y un momento`() {
        val viewModel = viewModel()
        assertFalse(viewModel.uiState.value.canContinue(OnboardingStep.Goals))
        assertFalse(viewModel.uiState.value.canContinue(OnboardingStep.Moment))

        viewModel.onGoalsChange(listOf(Goal.FOCUS))
        viewModel.onRiskMomentsChange(setOf(RiskMoment.BORED))

        assertTrue(viewModel.uiState.value.canContinue(OnboardingStep.Goals))
        assertTrue(viewModel.uiState.value.canContinue(OnboardingStep.Moment))
    }

    @Test
    fun `el uso diario parte del tramo central`() {
        assertEquals(UsageRange.FROM_4_TO_6, viewModel().uiState.value.usageRange)
    }

    @Test
    fun `el rigor sugerido sigue a las respuestas`() {
        val viewModel = viewModel()
        assertEquals(RigorLevel.MINDFUL, viewModel.uiState.value.suggestedRigor)

        viewModel.onUsageRangeChange(UsageRange.OVER_8)
        assertEquals(RigorLevel.STRICT, viewModel.uiState.value.suggestedRigor)

        viewModel.onUsageRangeChange(UsageRange.UNDER_2)
        assertEquals(RigorLevel.MINDFUL, viewModel.uiState.value.suggestedRigor)
    }

    // Sonido

    @Test
    fun `el sonido empieza encendido y se puede apagar`() {
        val viewModel = viewModel()
        assertTrue(viewModel.uiState.value.soundEnabled)

        viewModel.onSoundToggle()

        assertFalse(viewModel.uiState.value.soundEnabled)
    }

    // Compromiso

    @Test
    fun `el compromiso guarda el perfil y completa el onboarding`() = runTest {
        val viewModel = viewModel()
        viewModel.onNameChange("  Ana  ")
        viewModel.onGoalsChange(listOf(Goal.FOCUS, Goal.SLEEP))
        viewModel.onUsageRangeChange(UsageRange.FROM_6_TO_8)
        viewModel.onRiskMomentsChange(setOf(RiskMoment.BEFORE_SLEEP))

        assertTrue(viewModel.commit())

        assertEquals(
            UserProfile(
                name = "Ana",
                goals = listOf(Goal.FOCUS, Goal.SLEEP),
                usageRange = UsageRange.FROM_6_TO_8,
                riskMoments = setOf(RiskMoment.BEFORE_SLEEP),
                suggestedRigor = RigorLevel.STRICT
            ),
            profiles.saved
        )
        assertTrue(preferences.isOnboardingCompleted.first())
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `sin un perfil valido el compromiso no guarda nada`() = runTest {
        val viewModel = viewModel()
        viewModel.onNameChange("Ana")

        assertFalse(viewModel.commit())

        assertNull(profiles.saved)
        assertFalse(preferences.isOnboardingCompleted.first())
    }

    @Test
    fun `si el guardado falla se puede reintentar`() = runTest {
        val viewModel = viewModel()
        viewModel.answerEverything()
        profiles.failNextSave = true

        assertFalse(viewModel.commit())
        assertFalse(preferences.isOnboardingCompleted.first())
        assertTrue(viewModel.uiState.value.canContinue(OnboardingStep.Commit))

        assertTrue(viewModel.commit())
        assertTrue(preferences.isOnboardingCompleted.first())
    }

    @Test
    fun `si el usuario vuelve y cambia algo, el perfil se guarda otra vez al día`() = runTest {
        val viewModel = viewModel()
        viewModel.answerEverything()
        viewModel.commit()

        viewModel.onNameChange("Bea")
        viewModel.commit()

        assertEquals("Bea", profiles.saved?.name)
    }

    // Estado guardado

    @Test
    fun `el paso y las respuestas sobreviven a la muerte del proceso`() {
        val savedStateHandle = SavedStateHandle()
        viewModel(savedStateHandle).apply {
            onStepChange(OnboardingStep.Plan)
            onNameChange("Ana")
            onGoalsChange(listOf(Goal.SLEEP, Goal.FOCUS))
            onUsageRangeChange(UsageRange.OVER_8)
            onRiskMomentsChange(setOf(RiskMoment.WAKING_UP))
            onSoundToggle()
        }

        val restored = viewModel(savedStateHandle).uiState.value

        assertEquals(OnboardingStep.Plan, restored.step)
        assertEquals("Ana", restored.name)
        assertEquals(listOf(Goal.SLEEP, Goal.FOCUS), restored.goals)
        assertEquals(UsageRange.OVER_8, restored.usageRange)
        assertEquals(setOf(RiskMoment.WAKING_UP), restored.riskMoments)
        assertEquals(RigorLevel.STRICT, restored.suggestedRigor)
        assertFalse(restored.soundEnabled)
        assertTrue(restored.isProfileValid)
    }
}
