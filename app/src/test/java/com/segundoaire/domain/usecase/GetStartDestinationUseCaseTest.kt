package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.model.StartDestination
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.testing.FakeUserPreferencesRepository
import com.segundoaire.testing.FakeUserProfileRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetStartDestinationUseCaseTest {

    private val resolve = GetStartDestinationUseCase.Companion::resolve

    @Test
    fun `sin onboarding entra por el onboarding aunque el perfil tenga cuenta`() {
        val profile = UserProfile(authMode = AuthMode.GUEST, setupCompleted = true)

        assertEquals(StartDestination.ONBOARDING, resolve(false, profile))
        assertEquals(StartDestination.ONBOARDING, resolve(false, UserProfile()))
    }

    @Test
    fun `con onboarding y sin cuenta entra por la cuenta`() {
        assertEquals(StartDestination.ACCOUNT, resolve(true, UserProfile()))
    }

    @Test
    fun `con cuenta y sin setup entra por los permisos`() {
        val profile = UserProfile(authMode = AuthMode.EMAIL, email = "ana@correo.com")

        assertEquals(StartDestination.SETUP, resolve(true, profile))
    }

    @Test
    fun `con todo completo entra al panel`() {
        val profile = UserProfile(authMode = AuthMode.GOOGLE, setupCompleted = true)

        assertEquals(StartDestination.MAIN, resolve(true, profile))
    }

    @Test
    fun `todos los modos de cuenta, incluido continuar sin cuenta, pasan al setup`() {
        AuthMode.entries.forEach { mode ->
            assertEquals(StartDestination.SETUP, resolve(true, UserProfile(authMode = mode)))
        }
    }

    @Test
    fun `el setup completo sin cuenta vuelve a pedir la cuenta`() {
        // Perfil heredado o corrupto: manda el primer paso que falta, no el último hecho
        assertEquals(StartDestination.ACCOUNT, resolve(true, UserProfile(setupCompleted = true)))
    }

    @Test
    fun `el flujo lee ambos repositorios y sigue sus cambios`() = runTest {
        val preferences = FakeUserPreferencesRepository()
        val profiles = FakeUserProfileRepository()
        val useCase = GetStartDestinationUseCase(preferences, profiles)
        assertEquals(StartDestination.ONBOARDING, useCase().first())

        preferences.completeOnboarding()
        assertEquals(StartDestination.ACCOUNT, useCase().first())

        profiles.saveAccount(AuthMode.GUEST, email = "")
        assertEquals(StartDestination.SETUP, useCase().first())

        profiles.completeSetup()
        assertEquals(StartDestination.MAIN, useCase().first())
    }
}
