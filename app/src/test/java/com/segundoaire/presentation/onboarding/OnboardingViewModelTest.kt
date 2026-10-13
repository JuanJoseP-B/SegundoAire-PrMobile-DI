package com.segundoaire.presentation.onboarding

import androidx.lifecycle.SavedStateHandle
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.RiskMoment
import com.segundoaire.domain.model.UsageRange
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.domain.repository.UserPreferencesRepository
import com.segundoaire.domain.usecase.CalculateScreenTimeProjectionUseCase
import com.segundoaire.domain.usecase.CompleteOnboardingUseCase
import com.segundoaire.domain.usecase.SaveUserProfileUseCase
import com.segundoaire.domain.usecase.SuggestRigorUseCase
import com.segundoaire.testing.FakeUserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val profiles = FakeUserProfileRepository()
    private val preferences = FakeUserPreferencesRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) = OnboardingViewModel(
        savedStateHandle = savedStateHandle,
        suggestRigor = SuggestRigorUseCase(),
        calculateProjection = CalculateScreenTimeProjectionUseCase(),
        saveUserProfile = SaveUserProfileUseCase(profiles),
        completeOnboarding = CompleteOnboardingUseCase(preferences)
    )

    private fun OnboardingViewModel.answerEverything() {
        onNameChange("Ana")
        onGoalsChange(listOf(Goal.SLEEP))
        onUsageRangeChange(UsageRange.FROM_2_TO_4)
        onRiskMomentsChange(setOf(RiskMoment.BEFORE_SLEEP))
    }

    // Bienvenida

    @Test
    fun `la bienvenida avanza y retrocede sin salirse de sus tres tarjetas`() {
        val viewModel = viewModel()

        viewModel.onWelcomeBack()
        assertEquals(0, viewModel.uiState.value.welcomePage)

        repeat(5) { viewModel.onWelcomeNext() }
        assertEquals(2, viewModel.uiState.value.welcomePage)
        assertTrue(viewModel.uiState.value.isLastWelcomePage)

        viewModel.onWelcomeBack()
        assertEquals(1, viewModel.uiState.value.welcomePage)
    }

    // Nombre

    @Test
    fun `sin nombre no se puede continuar y no hay error`() {
        val state = viewModel().uiState.value

        assertFalse(state.canContinue(OnboardingStep.NAME))
        assertNull(state.nameError)
    }

    @Test
    fun `un nombre solo de espacios no alcanza`() {
        val viewModel = viewModel()

        viewModel.onNameChange("   ")

        assertFalse(viewModel.uiState.value.canContinue(OnboardingStep.NAME))
    }

    @Test
    fun `acepta nombres con tildes, guion y apostrofo`() {
        val viewModel = viewModel()

        listOf("José", "Ana-María", "O'Connor", "María José").forEach { name ->
            viewModel.onNameChange(name)

            assertNull(name, viewModel.uiState.value.nameError)
            assertTrue(name, viewModel.uiState.value.canContinue(OnboardingStep.NAME))
        }
    }

    @Test
    fun `un nombre con numeros o simbolos marca error`() {
        val viewModel = viewModel()

        viewModel.onNameChange("Ana99")

        assertEquals(NameError.INVALID_CHARACTERS, viewModel.uiState.value.nameError)
        assertFalse(viewModel.uiState.value.canContinue(OnboardingStep.NAME))
    }

    @Test
    fun `corregir el nombre quita el error`() {
        val viewModel = viewModel()

        viewModel.onNameChange("Ana@")
        viewModel.onNameChange("Ana")

        assertNull(viewModel.uiState.value.nameError)
    }

    @Test
    fun `el nombre se corta en el largo maximo`() {
        val viewModel = viewModel()

        viewModel.onNameChange("a".repeat(UserProfile.MAX_NAME_LENGTH + 10))

        assertEquals(UserProfile.MAX_NAME_LENGTH, viewModel.uiState.value.name.length)
    }

    // Objetivos

    @Test
    fun `hace falta al menos un objetivo`() {
        val viewModel = viewModel()
        assertFalse(viewModel.uiState.value.canContinue(OnboardingStep.GOALS))

        viewModel.onGoalsChange(listOf(Goal.FOCUS))

        assertTrue(viewModel.uiState.value.canContinue(OnboardingStep.GOALS))
    }

    @Test
    fun `los objetivos se limitan a dos y conservan el orden`() {
        val viewModel = viewModel()

        viewModel.onGoalsChange(listOf(Goal.PRESENCE, Goal.FOCUS, Goal.SLEEP))

        assertEquals(listOf(Goal.PRESENCE, Goal.FOCUS), viewModel.uiState.value.goals)
    }

    // Uso y momentos

    @Test
    fun `el uso diario parte del tramo central y siempre deja continuar`() {
        val state = viewModel().uiState.value

        assertEquals(UsageRange.FROM_4_TO_6, state.usageRange)
        assertTrue(state.canContinue(OnboardingStep.USAGE))
    }

    @Test
    fun `cambiar el uso diario recalcula la proyeccion`() {
        val viewModel = viewModel()

        viewModel.onUsageRangeChange(UsageRange.FROM_2_TO_4)

        assertEquals(3, viewModel.uiState.value.projection.hoursPerDay)
        assertEquals(50, viewModel.uiState.value.projection.screenYearsTenths)
    }

    @Test
    fun `hace falta al menos un momento de mayor uso`() {
        val viewModel = viewModel()
        assertFalse(viewModel.uiState.value.canContinue(OnboardingStep.MOMENTS))

        viewModel.onRiskMomentsChange(setOf(RiskMoment.BORED))

        assertTrue(viewModel.uiState.value.canContinue(OnboardingStep.MOMENTS))
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

    // Respiración

    @Test
    fun `la respiracion termina al tercer ciclo`() {
        val viewModel = viewModel()

        repeat(2) { viewModel.onBreathCycleCompleted() }
        assertFalse(viewModel.uiState.value.isBreathingDone)

        repeat(3) { viewModel.onBreathCycleCompleted() }
        assertTrue(viewModel.uiState.value.isBreathingDone)
        assertEquals(OnboardingUiState.BREATH_CYCLES, viewModel.uiState.value.breathCyclesDone)
    }

    // Compromiso

    @Test
    fun `el compromiso guarda el perfil y completa el onboarding`() {
        val viewModel = viewModel()
        viewModel.onNameChange("  Ana  ")
        viewModel.onGoalsChange(listOf(Goal.FOCUS, Goal.SLEEP))
        viewModel.onUsageRangeChange(UsageRange.FROM_6_TO_8)
        viewModel.onRiskMomentsChange(setOf(RiskMoment.BEFORE_SLEEP))

        viewModel.onCommit()

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
        assertTrue(preferences.completed.value)
        assertTrue(viewModel.uiState.value.isCompleted)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `sin un perfil valido el compromiso no guarda nada`() {
        val viewModel = viewModel()
        viewModel.onNameChange("Ana")

        viewModel.onCommit()

        assertNull(profiles.saved)
        assertFalse(preferences.completed.value)
        assertFalse(viewModel.uiState.value.isCompleted)
    }

    @Test
    fun `comprometerse dos veces guarda una sola vez`() {
        val viewModel = viewModel()
        viewModel.answerEverything()

        viewModel.onCommit()
        viewModel.onCommit()

        assertEquals(1, profiles.saveCount)
    }

    @Test
    fun `si el guardado falla se puede reintentar`() {
        val viewModel = viewModel()
        viewModel.answerEverything()
        profiles.failNextSave = true

        viewModel.onCommit()
        assertFalse(viewModel.uiState.value.isCompleted)
        assertFalse(preferences.completed.value)
        assertTrue(viewModel.uiState.value.canContinue(OnboardingStep.COMMITMENT))

        viewModel.onCommit()
        assertTrue(viewModel.uiState.value.isCompleted)
    }

    // Estado guardado

    @Test
    fun `las respuestas sobreviven a la muerte del proceso`() {
        val savedStateHandle = SavedStateHandle()
        viewModel(savedStateHandle).apply {
            onNameChange("Ana")
            onGoalsChange(listOf(Goal.SLEEP, Goal.FOCUS))
            onUsageRangeChange(UsageRange.OVER_8)
            onRiskMomentsChange(setOf(RiskMoment.WAKING_UP))
        }

        val restored = viewModel(savedStateHandle).uiState.value

        assertEquals("Ana", restored.name)
        assertEquals(listOf(Goal.SLEEP, Goal.FOCUS), restored.goals)
        assertEquals(UsageRange.OVER_8, restored.usageRange)
        assertEquals(setOf(RiskMoment.WAKING_UP), restored.riskMoments)
        assertEquals(RigorLevel.STRICT, restored.suggestedRigor)
        assertEquals(8, restored.projection.hoursPerDay)
        assertTrue(restored.isProfileValid)
    }
}

private class FakeUserPreferencesRepository : UserPreferencesRepository {
    val completed = MutableStateFlow(false)

    override val isOnboardingCompleted: Flow<Boolean> = completed

    override suspend fun completeOnboarding() {
        completed.value = true
    }
}
