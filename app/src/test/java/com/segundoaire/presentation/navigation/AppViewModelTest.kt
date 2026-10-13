package com.segundoaire.presentation.navigation

import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.model.StartDestination
import com.segundoaire.domain.usecase.GetStartDestinationUseCase
import com.segundoaire.testing.FakeUserPreferencesRepository
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val preferences = FakeUserPreferencesRepository()
    private val profiles = FakeUserProfileRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = AppViewModel(GetStartDestinationUseCase(preferences, profiles))

    @Test
    fun `no hay destino hasta que se leen las preferencias`() = runTest(dispatcher) {
        val viewModel = viewModel()

        assertNull(viewModel.startDestination.value)

        advanceUntilIdle()
        assertEquals(StartDestination.ONBOARDING, viewModel.startDestination.value)
    }

    @Test
    fun `un perfil a medias entra por el primer paso que falta`() = runTest(dispatcher) {
        preferences.completeOnboarding()
        profiles.saveAccount(AuthMode.EMAIL, email = "ana@correo.com")
        val viewModel = viewModel()

        advanceUntilIdle()

        assertEquals(StartDestination.SETUP, viewModel.startDestination.value)
    }

    @Test
    fun `el destino sigue el avance mientras la app esta abierta`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        preferences.completeOnboarding()
        profiles.saveAccount(AuthMode.GUEST, email = "")
        profiles.completeSetup()
        advanceUntilIdle()

        assertEquals(StartDestination.MAIN, viewModel.startDestination.value)
    }
}
