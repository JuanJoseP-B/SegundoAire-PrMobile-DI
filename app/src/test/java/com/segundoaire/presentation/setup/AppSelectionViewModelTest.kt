package com.segundoaire.presentation.setup

import com.segundoaire.domain.model.BlockedApp
import com.segundoaire.domain.model.InstalledApp
import com.segundoaire.domain.usecase.CompleteSetupUseCase
import com.segundoaire.domain.usecase.GetBlockedAppsUseCase
import com.segundoaire.domain.usecase.GetInstalledAppsUseCase
import com.segundoaire.testing.FakeBlockedAppRepository
import com.segundoaire.testing.FakeInstalledAppsRepository
import com.segundoaire.testing.FakeUserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppSelectionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val installedApps = FakeInstalledAppsRepository(
        listOf(
            InstalledApp("com.example.calc", "Calculadora"),
            InstalledApp("com.instagram.android", "Instagram"),
            InstalledApp("com.example.maps", "Mapas"),
            InstalledApp("com.roblox.client", "Roblox")
        )
    )
    private val blockedApps = FakeBlockedAppRepository(
        listOf(
            BlockedApp("com.example.maps", "Mapas"),
            BlockedApp("com.roblox.client", "Roblox", isBlocked = false),
            // Ya no está instalada: no puede quedar marcada
            BlockedApp("com.example.removed", "Vieja")
        )
    )
    private val profile = FakeUserProfileRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = AppSelectionViewModel(
        GetInstalledAppsUseCase(installedApps),
        CompleteSetupUseCase(blockedApps, profile),
        GetBlockedAppsUseCase(blockedApps),
        AppIconLoader { null }
    )

    @Test
    fun `empieza cargando y luego separa sugeridas del resto`() = runTest(dispatcher) {
        val viewModel = viewModel()
        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.hasInstalledApps)
        assertEquals(listOf("Instagram", "Roblox"), state.suggested.map { it.appName })
        assertEquals(listOf("Calculadora", "Mapas"), state.apps.map { it.appName })
    }

    @Test
    fun `sin apps instaladas deja el estado vacio`() = runTest(dispatcher) {
        installedApps.apps = emptyList()
        val viewModel = viewModel()

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.hasInstalledApps)
        assertFalse(viewModel.uiState.value.hasResults)
    }

    @Test
    fun `si la consulta falla termina de cargar sin apps`() = runTest(dispatcher) {
        installedApps.failNextQuery = true
        val viewModel = viewModel()

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.hasInstalledApps)
    }

    @Test
    fun `la busqueda ignora mayusculas y devuelve una sola lista`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("  mA ")
        val state = viewModel.uiState.value

        assertTrue(state.isSearching)
        assertTrue(state.suggested.isEmpty())
        assertEquals(listOf("Mapas"), state.apps.map { it.appName })
    }

    @Test
    fun `la busqueda incluye las sugeridas que coinciden`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("insta")

        assertEquals(listOf("Instagram"), viewModel.uiState.value.apps.map { it.appName })
    }

    @Test
    fun `sin coincidencias no hay resultados pero si hay apps instaladas`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("zzz")

        assertFalse(viewModel.uiState.value.hasResults)
        assertTrue(viewModel.uiState.value.hasInstalledApps)
    }

    @Test
    fun `borrar la busqueda recupera las secciones`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onQueryChange("mapas")

        viewModel.onQueryChange("")

        assertFalse(viewModel.uiState.value.isSearching)
        assertEquals(2, viewModel.uiState.value.suggested.size)
        assertEquals(2, viewModel.uiState.value.apps.size)
    }

    @Test
    fun `el contador sigue las apps elegidas`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.selectedCount)

        viewModel.onToggle("com.example.maps", true)
        viewModel.onToggle("com.instagram.android", true)
        assertEquals(2, viewModel.uiState.value.selectedCount)

        viewModel.onToggle("com.example.maps", false)
        assertEquals(setOf("com.instagram.android"), viewModel.uiState.value.selectedPackages)
    }

    @Test
    fun `sin apps elegidas no se puede confirmar`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canConfirm)

        viewModel.onConfirm()
        advanceUntilIdle()

        assertEquals(0, blockedApps.replaceCount)
        assertFalse(viewModel.uiState.value.isCompleted)
    }

    @Test
    fun `confirmar guarda las elegidas en Room y marca el setup`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onToggle("com.example.maps", true)
        viewModel.onToggle("com.roblox.client", true)

        viewModel.onConfirm()
        advanceUntilIdle()

        assertEquals(
            setOf(BlockedApp("com.example.maps", "Mapas"), BlockedApp("com.roblox.client", "Roblox")),
            blockedApps.current.toSet()
        )
        assertTrue(profile.saved?.setupCompleted == true)
        assertTrue(viewModel.uiState.value.isCompleted)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `una app elegida y ocultada por la busqueda tambien se guarda`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onToggle("com.example.calc", true)
        viewModel.onQueryChange("roblox")

        viewModel.onConfirm()
        advanceUntilIdle()

        assertEquals(listOf("com.example.calc"), blockedApps.current.map { it.packageName })
    }

    @Test
    fun `un segundo toque en confirmar no guarda dos veces`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onToggle("com.example.maps", true)

        viewModel.onConfirm()
        viewModel.onConfirm()
        advanceUntilIdle()

        assertEquals(1, blockedApps.replaceCount)
    }

    @Test
    fun `si guardar falla conserva la seleccion y permite reintentar`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onToggle("com.example.maps", true)
        blockedApps.failNextReplace = true

        viewModel.onConfirm()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.saveFailed)
        assertFalse(viewModel.uiState.value.isCompleted)
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals(setOf("com.example.maps"), viewModel.uiState.value.selectedPackages)
        assertTrue(profile.saved?.setupCompleted != true)

        viewModel.onConfirm()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isCompleted)
        assertFalse(viewModel.uiState.value.saveFailed)
        assertTrue(profile.saved?.setupCompleted == true)
    }

    @Test
    fun `tras completar ya no se puede cambiar la seleccion`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onToggle("com.example.maps", true)
        viewModel.onConfirm()
        advanceUntilIdle()

        viewModel.onToggle("com.example.calc", true)

        assertEquals(setOf("com.example.maps"), viewModel.uiState.value.selectedPackages)
    }

    @Test
    fun `al editar marca las apps vigiladas e instaladas`() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.onEditStarted()
        advanceUntilIdle()

        // Roblox está apagada y "Vieja" no está instalada: ninguna queda marcada
        assertEquals(setOf("com.example.maps"), viewModel.uiState.value.selectedPackages)
        assertTrue(viewModel.uiState.value.canConfirm)
    }

    @Test
    fun `el modo edicion solo marca una vez`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.onEditStarted()
        advanceUntilIdle()
        viewModel.onToggle("com.example.maps", false)

        viewModel.onEditStarted()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.selectedPackages.isEmpty())
    }

    @Test
    fun `al confirmar una edicion reemplaza la lista y conserva el cooldown`() = runTest(dispatcher) {
        blockedApps.upsert(BlockedApp("com.example.maps", "Mapas", cooldownMinutes = 15))
        val viewModel = viewModel()
        viewModel.onEditStarted()
        advanceUntilIdle()
        viewModel.onToggle("com.example.calc", true)

        viewModel.onConfirm()
        advanceUntilIdle()

        assertEquals(
            setOf(
                BlockedApp("com.example.maps", "Mapas", cooldownMinutes = 15),
                BlockedApp("com.example.calc", "Calculadora")
            ),
            blockedApps.current.toSet()
        )
    }
}
