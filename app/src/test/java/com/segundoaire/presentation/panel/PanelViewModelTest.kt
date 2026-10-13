package com.segundoaire.presentation.panel

import com.segundoaire.domain.model.BlockedApp
import com.segundoaire.domain.model.Goal
import com.segundoaire.domain.model.GreetingPeriod
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.domain.usecase.CheckAccessibilityEnabledUseCase
import com.segundoaire.domain.usecase.CheckOverlayPermissionUseCase
import com.segundoaire.domain.usecase.GetBlockedAppsUseCase
import com.segundoaire.domain.usecase.GetGreetingPeriodUseCase
import com.segundoaire.domain.usecase.GetUserProfileUseCase
import com.segundoaire.domain.usecase.ToggleAppBlockStatusUseCase
import com.segundoaire.presentation.setup.AppIconLoader
import com.segundoaire.testing.FakeAccessibilityStatusRepository
import com.segundoaire.testing.FakeBlockedAppRepository
import com.segundoaire.testing.FakeMotivationalMessageProvider
import com.segundoaire.testing.FakeOverlayPermissionRepository
import com.segundoaire.testing.FakeUserProfileRepository
import com.segundoaire.testing.MutableClock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
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

@OptIn(ExperimentalCoroutinesApi::class)
class PanelViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val blockedApps = FakeBlockedAppRepository(
        listOf(
            BlockedApp("com.example.video", "Videos"),
            BlockedApp("com.example.social", "Social"),
            BlockedApp("com.example.news", "Noticias", isBlocked = false)
        )
    )
    private val profile = FakeUserProfileRepository()
    private val accessibility = FakeAccessibilityStatusRepository(enabled = true)
    private val overlay = FakeOverlayPermissionRepository(granted = true)
    private val clock = MutableClock()
    private val messages = FakeMotivationalMessageProvider()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = PanelViewModel(
        GetBlockedAppsUseCase(blockedApps),
        GetUserProfileUseCase(profile),
        GetGreetingPeriodUseCase(clock),
        ToggleAppBlockStatusUseCase(blockedApps),
        CheckOverlayPermissionUseCase(overlay),
        CheckAccessibilityEnabledUseCase(accessibility),
        messages,
        AppIconLoader { null }
    )

    // uiState usa WhileSubscribed: sin un colector no hay estado nuevo
    private fun TestScope.observed(viewModel: PanelViewModel): PanelViewModel {
        backgroundScope.launch(dispatcher) { viewModel.uiState.collect {} }
        return viewModel
    }

    @Test
    fun `empieza cargando y luego muestra las apps y el perfil`() = runTest(dispatcher) {
        profile.saveProfile(UserProfile(name = "Ana", goals = listOf(Goal.SLEEP, Goal.FOCUS)))
        val viewModel = viewModel()
        assertTrue(viewModel.uiState.value.isLoading)

        observed(viewModel)
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals("Ana", state.name)
        assertEquals(Goal.SLEEP, state.mainGoal)
        assertEquals(listOf("Videos", "Social", "Noticias"), state.apps.map { it.appName })
    }

    @Test
    fun `cuenta solo las apps vigiladas`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())

        assertEquals(2, viewModel.uiState.value.watchedAppsCount)
        assertTrue(viewModel.uiState.value.hasApps)
    }

    @Test
    fun `sin apps no hay lista que mostrar`() = runTest(dispatcher) {
        blockedApps.replaceAll(emptyList())
        val viewModel = observed(viewModel())

        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.hasApps)
        assertEquals(0, viewModel.uiState.value.watchedAppsCount)
    }

    // --- Saludo por hora ---

    @Test
    fun `saluda segun la hora del reloj`() = runTest(dispatcher) {
        val expected = mapOf(
            8 to GreetingPeriod.MORNING,
            15 to GreetingPeriod.AFTERNOON,
            22 to GreetingPeriod.EVENING,
            3 to GreetingPeriod.EVENING
        )
        expected.forEach { (hour, period) ->
            clock.setHour(hour)
            val viewModel = observed(viewModel())

            assertEquals("a las $hour h", period, viewModel.uiState.value.greeting)
        }
    }

    @Test
    fun `al volver a la app actualiza el saludo`() = runTest(dispatcher) {
        clock.setHour(9)
        val viewModel = observed(viewModel())
        assertEquals(GreetingPeriod.MORNING, viewModel.uiState.value.greeting)

        clock.setHour(21)
        viewModel.onResume()

        assertEquals(GreetingPeriod.EVENING, viewModel.uiState.value.greeting)
    }

    // --- Permisos ---

    @Test
    fun `con ambos permisos no falta ninguno`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())

        assertNull(viewModel.uiState.value.missingPermission)
    }

    @Test
    fun `sin accesibilidad pide primero accesibilidad`() = runTest(dispatcher) {
        accessibility.enabled = false
        overlay.granted = false
        val viewModel = observed(viewModel())

        assertEquals(MissingPermission.ACCESSIBILITY, viewModel.uiState.value.missingPermission)
    }

    @Test
    fun `con accesibilidad pero sin superposicion pide superposicion`() = runTest(dispatcher) {
        overlay.granted = false
        val viewModel = observed(viewModel())

        assertEquals(MissingPermission.OVERLAY, viewModel.uiState.value.missingPermission)
    }

    @Test
    fun `al volver de Ajustes el permiso concedido deja de faltar`() = runTest(dispatcher) {
        accessibility.enabled = false
        val viewModel = observed(viewModel())
        assertEquals(MissingPermission.ACCESSIBILITY, viewModel.uiState.value.missingPermission)

        accessibility.enabled = true
        viewModel.onResume()

        assertNull(viewModel.uiState.value.missingPermission)
    }

    @Test
    fun `refleja un permiso revocado al volver`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())

        overlay.granted = false
        viewModel.onResume()

        assertEquals(MissingPermission.OVERLAY, viewModel.uiState.value.missingPermission)
    }

    // --- Entrada escalonada ---

    @Test
    fun `al inicio ninguna app tiene la entrada marcada como mostrada`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())

        assertTrue(viewModel.uiState.value.animatedPackages.isEmpty())
    }

    @Test
    fun `las apps animadas quedan marcadas y se acumulan`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())

        viewModel.onAppsAnimated(listOf("com.example.video", "com.example.social"))
        viewModel.onAppsAnimated(listOf("com.example.news", "com.example.video"))

        assertEquals(
            setOf("com.example.video", "com.example.social", "com.example.news"),
            viewModel.uiState.value.animatedPackages
        )
    }

    @Test
    fun `una app nueva no esta marcada y por eso se anima`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())
        viewModel.onAppsAnimated(listOf("com.example.video"))

        blockedApps.upsert(BlockedApp("com.example.maps", "Mapas"))

        assertFalse("com.example.maps" in viewModel.uiState.value.animatedPackages)
        assertTrue("com.example.video" in viewModel.uiState.value.animatedPackages)
    }

    // --- Apps ---

    @Test
    fun `apagar una app la guarda en el repositorio`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())

        viewModel.onToggleApp("com.example.video", false)

        assertEquals(1, viewModel.uiState.value.watchedAppsCount)
        assertFalse(blockedApps.current.first { it.packageName == "com.example.video" }.isBlocked)
    }

    // --- Mensaje de la IA simulada ---

    @Test
    fun `pide el primer mensaje con el objetivo principal`() = runTest(dispatcher) {
        profile.saveProfile(UserProfile(goals = listOf(Goal.PRESENCE)))
        val viewModel = observed(viewModel())

        assertEquals("uno", viewModel.uiState.value.message)
        assertFalse(viewModel.uiState.value.isThinking)
        assertEquals(listOf<Pair<Goal?, String?>>(Goal.PRESENCE to null), messages.calls)
    }

    @Test
    fun `mientras piensa lo indica y no tiene mensaje`() = runTest(dispatcher) {
        messages.gate = CompletableDeferred()
        val viewModel = observed(viewModel())

        assertTrue(viewModel.uiState.value.isThinking)
        assertNull(viewModel.uiState.value.message)

        messages.gate!!.complete(Unit)

        assertFalse(viewModel.uiState.value.isThinking)
        assertEquals("uno", viewModel.uiState.value.message)
    }

    @Test
    fun `otro mensaje se pide con el anterior para no repetirlo`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())

        viewModel.onNewMessage()

        assertEquals("dos", viewModel.uiState.value.message)
        assertEquals("uno", messages.calls.last().second)
    }

    @Test
    fun `no se piden mensajes nuevos mientras ya piensa uno`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())
        messages.gate = CompletableDeferred()
        viewModel.onNewMessage()
        assertTrue(viewModel.uiState.value.isThinking)

        viewModel.onNewMessage()
        viewModel.onNewMessage()
        messages.gate!!.complete(Unit)

        // La inicial y una sola petición más
        assertEquals(2, messages.calls.size)
        assertEquals("dos", viewModel.uiState.value.message)
    }

    @Test
    fun `si el proveedor falla conserva el mensaje anterior`() = runTest(dispatcher) {
        val viewModel = observed(viewModel())
        messages.failNext = true

        viewModel.onNewMessage()

        assertEquals("uno", viewModel.uiState.value.message)
        assertFalse(viewModel.uiState.value.isThinking)
    }

    @Test
    fun `si falla el primero la tarjeta queda sin mensaje y sin pensar`() = runTest(dispatcher) {
        messages.failNext = true
        val viewModel = observed(viewModel())

        assertNull(viewModel.uiState.value.message)
        assertFalse(viewModel.uiState.value.isThinking)
    }
}
