package com.segundoaire.presentation.profile

import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.RigorLevel
import com.segundoaire.domain.model.TreeState
import com.segundoaire.domain.model.UsageRange
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.domain.usecase.CheckAccessibilityEnabledUseCase
import com.segundoaire.domain.usecase.CheckOverlayPermissionUseCase
import com.segundoaire.domain.usecase.GetUserProfileUseCase
import com.segundoaire.domain.usecase.MinutosPorDiaUseCase
import com.segundoaire.domain.usecase.SaveUserProfileUseCase
import com.segundoaire.presentation.onboarding.NameError
import com.segundoaire.presentation.panel.MissingPermission
import com.segundoaire.testing.FakeAccessibilityStatusRepository
import com.segundoaire.testing.FakeBlockSessionRepository
import com.segundoaire.testing.FakeOverlayPermissionRepository
import com.segundoaire.testing.FakeTreeRepository
import com.segundoaire.testing.FakeUserPreferencesRepository
import com.segundoaire.testing.FakeUserProfileRepository
import com.segundoaire.testing.MutableClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeUserProfileRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val clock = MutableClock(Instant.parse("2026-10-16T10:00:00Z"))
    private val trees = FakeTreeRepository()
    private val preferences = FakeUserPreferencesRepository()
    private val sessions = FakeBlockSessionRepository()
    private val accessibility = FakeAccessibilityStatusRepository(enabled = true)
    private val overlay = FakeOverlayPermissionRepository(granted = true)

    private fun viewModel() = ProfileViewModel(
        GetUserProfileUseCase(repository),
        SaveUserProfileUseCase(repository),
        trees,
        preferences,
        MinutosPorDiaUseCase(sessions, clock),
        CheckOverlayPermissionUseCase(overlay),
        CheckAccessibilityEnabledUseCase(accessibility),
        clock
    )

    // El perfil con el que ya salió del onboarding y del setup
    private suspend fun seedProfile(): Int {
        repository.saveProfile(
            UserProfile(
                name = "Ana",
                goals = listOf(Goal.SLEEP),
                usageRange = UsageRange.FROM_6_TO_8,
                authMode = AuthMode.EMAIL,
                email = "ana@correo.com"
            )
        )
        repository.completeSetup()
        return repository.saveCount
    }

    @Test
    fun `un perfil recién creado carga con lo guardado`() = runTest(dispatcher) {
        seedProfile()

        val state = viewModel().uiState.value

        assertFalse(state.isLoading)
        assertEquals("Ana", state.name)
        assertEquals(listOf(Goal.SLEEP), state.goals)
        assertEquals(AuthMode.EMAIL, state.authMode)
        assertEquals("ana@correo.com", state.email)
        assertFalse(state.hasChanges)
        assertFalse(state.canSave)
    }

    @Test
    fun `editar el nombre marca cambios y permite guardar`() = runTest(dispatcher) {
        seedProfile()
        val viewModel = viewModel()

        viewModel.onNameChange("Ana María")

        val state = viewModel.uiState.value
        assertTrue(state.hasChanges)
        assertTrue(state.canSave)
    }

    @Test
    fun `volver al valor guardado quita los cambios`() = runTest(dispatcher) {
        seedProfile()
        val viewModel = viewModel()

        viewModel.onNameChange("Ana María")
        viewModel.onNameChange("Ana")

        assertFalse(viewModel.uiState.value.hasChanges)
    }

    @Test
    fun `los espacios sobrantes no cuentan como cambio`() = runTest(dispatcher) {
        seedProfile()
        val viewModel = viewModel()

        viewModel.onNameChange("  Ana ")

        assertFalse(viewModel.uiState.value.hasChanges)
    }

    @Test
    fun `un nombre con números muestra el error y no se puede guardar`() = runTest(dispatcher) {
        seedProfile()
        val viewModel = viewModel()

        viewModel.onNameChange("Ana2")

        val state = viewModel.uiState.value
        assertEquals(NameError.INVALID_CHARACTERS, state.nameError)
        assertFalse(state.canSave)
    }

    @Test
    fun `un nombre vacío no se puede guardar`() = runTest(dispatcher) {
        seedProfile()
        val viewModel = viewModel()

        viewModel.onNameChange("   ")

        val state = viewModel.uiState.value
        assertTrue(state.hasChanges)
        assertFalse(state.canSave)
    }

    @Test
    fun `el nombre se recorta al máximo permitido`() = runTest(dispatcher) {
        seedProfile()
        val viewModel = viewModel()

        viewModel.onNameChange("a".repeat(UserProfile.MAX_NAME_LENGTH + 10))

        assertEquals(UserProfile.MAX_NAME_LENGTH, viewModel.uiState.value.name.length)
    }

    @Test
    fun `sin objetivos no se puede guardar`() = runTest(dispatcher) {
        seedProfile()
        val viewModel = viewModel()

        viewModel.onGoalsChange(emptyList())

        val state = viewModel.uiState.value
        assertTrue(state.hasChanges)
        assertFalse(state.canSave)
    }

    @Test
    fun `los objetivos conservan el orden y se limitan a dos`() = runTest(dispatcher) {
        seedProfile()
        val viewModel = viewModel()

        viewModel.onGoalsChange(listOf(Goal.PRESENCE, Goal.FOCUS, Goal.FREE_TIME))

        assertEquals(listOf(Goal.PRESENCE, Goal.FOCUS), viewModel.uiState.value.goals)
    }

    @Test
    fun `guardar escribe nombre y objetivos sin tocar el resto del perfil`() = runTest(dispatcher) {
        seedProfile()
        val viewModel = viewModel()

        viewModel.onNameChange("  Ana María ")
        viewModel.onGoalsChange(listOf(Goal.FOCUS, Goal.SLEEP))
        viewModel.onSave()

        val saved = repository.saved!!
        assertEquals("Ana María", saved.name)
        assertEquals(listOf(Goal.FOCUS, Goal.SLEEP), saved.goals)
        assertEquals(UsageRange.FROM_6_TO_8, saved.usageRange)
        assertEquals(AuthMode.EMAIL, saved.authMode)
        assertTrue(saved.setupCompleted)
    }

    @Test
    fun `tras guardar ya no hay cambios y se avisa`() = runTest(dispatcher) {
        seedProfile()
        val viewModel = viewModel()

        viewModel.onNameChange("Ana María")
        viewModel.onSave()

        val state = viewModel.uiState.value
        assertFalse(state.hasChanges)
        assertFalse(state.isSaving)
        assertFalse(state.canSave)
        assertEquals(SaveFeedback.SAVED, state.feedback)
    }

    @Test
    fun `guardar sin cambios no escribe`() = runTest(dispatcher) {
        val before = seedProfile()
        val viewModel = viewModel()

        viewModel.onSave()

        assertEquals(before, repository.saveCount)
    }

    @Test
    fun `si no se puede escribir se conserva el borrador y se puede reintentar`() = runTest(dispatcher) {
        seedProfile()
        val viewModel = viewModel()
        viewModel.onNameChange("Ana María")
        repository.failNextSave = true

        viewModel.onSave()

        val failed = viewModel.uiState.value
        assertEquals(SaveFeedback.FAILED, failed.feedback)
        assertEquals("Ana María", failed.name)
        assertTrue(failed.canSave)

        viewModel.onSave()

        assertEquals("Ana María", repository.saved!!.name)
        assertEquals(SaveFeedback.SAVED, viewModel.uiState.value.feedback)
    }

    @Test
    fun `editar de nuevo borra el aviso anterior`() = runTest(dispatcher) {
        seedProfile()
        val viewModel = viewModel()
        viewModel.onNameChange("Ana María")
        viewModel.onSave()

        viewModel.onNameChange("Ana Lucía")

        assertNull(viewModel.uiState.value.feedback)
    }

    @Test
    fun `un cambio externo no pisa lo que se está editando`() = runTest(dispatcher) {
        seedProfile()
        val viewModel = viewModel()
        viewModel.onNameChange("Ana María")

        repository.saveAccount(AuthMode.GOOGLE, "")

        val state = viewModel.uiState.value
        assertEquals("Ana María", state.name)
        assertEquals(AuthMode.GOOGLE, state.authMode)
    }

    @Test
    fun `sin borrador pendiente un cambio externo se refleja`() = runTest(dispatcher) {
        seedProfile()
        val viewModel = viewModel()

        repository.saveProfile(UserProfile(name = "Luis", goals = listOf(Goal.FREE_TIME)))

        val state = viewModel.uiState.value
        assertEquals("Luis", state.name)
        assertEquals(listOf(Goal.FREE_TIME), state.goals)
        assertFalse(state.hasChanges)
    }

    // --- Árbol, plan y modo, sonido y permisos ---

    @Test
    fun `muestra el arbol y el nombre guardado`() = runTest(dispatcher) {
        seedProfile()
        trees.tree.value = TreeState(goals = listOf(Goal.SLEEP), plantedAt = LocalDate.of(2026, 10, 1))
        val state = viewModel().uiState.value

        assertEquals("Ana", state.savedName)
        assertEquals(LocalDate.of(2026, 10, 1), state.tree.plantedAt)
    }

    @Test
    fun `plan y modo guarda el rigor sin tocar el resto del perfil`() = runTest(dispatcher) {
        seedProfile()
        val viewModel = viewModel()

        viewModel.onRigorChange(RigorLevel.STRICT)

        assertEquals(RigorLevel.STRICT, repository.saved?.suggestedRigor)
        assertEquals("Ana", repository.saved?.name)
        assertEquals(RigorLevel.STRICT, viewModel.uiState.value.rigor)
    }

    @Test
    fun `sonidos y vibracion se guarda en las preferencias`() = runTest(dispatcher) {
        val viewModel = viewModel()
        assertTrue(viewModel.uiState.value.soundEnabled)

        viewModel.onSoundChange(false)

        assertFalse(preferences.sound.value)
        assertFalse(viewModel.uiState.value.soundEnabled)
    }

    @Test
    fun `los permisos se releen al volver`() = runTest(dispatcher) {
        accessibility.enabled = false
        val viewModel = viewModel()
        assertEquals(MissingPermission.ACCESSIBILITY, viewModel.uiState.value.missingPermission)

        accessibility.enabled = true
        viewModel.onResume()

        assertEquals(null, viewModel.uiState.value.missingPermission)
    }

    @Test
    fun `abrir la edicion parte de lo guardado`() = runTest(dispatcher) {
        seedProfile()
        val viewModel = viewModel()
        viewModel.onNameChange("Otro")

        viewModel.onEditStart()

        assertEquals("Ana", viewModel.uiState.value.name)
        assertFalse(viewModel.uiState.value.hasChanges)
    }
}
