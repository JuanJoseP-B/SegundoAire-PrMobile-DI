package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.model.UserProfile
import com.segundoaire.testing.FakeUserProfileRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveAccountUseCaseTest {

    private val repository = FakeUserProfileRepository()
    private val saveAccount = SaveAccountUseCase(repository)

    @Test
    fun `guarda el correo normalizado`() = runTest {
        saveAccount(AuthMode.EMAIL, "  Ana@Correo.COM ")

        assertEquals(UserProfile(authMode = AuthMode.EMAIL, email = "ana@correo.com"), repository.saved)
    }

    @Test
    fun `los modos sin correo no guardan ninguno`() = runTest {
        listOf(AuthMode.GOOGLE, AuthMode.APPLE, AuthMode.GUEST).forEach { mode ->
            saveAccount(mode, "ana@correo.com")

            assertEquals(mode.name, UserProfile(authMode = mode), repository.saved)
        }
    }

    @Test
    fun `conserva el perfil que ya estaba guardado`() = runTest {
        repository.saveProfile(UserProfile(name = "Ana"))

        saveAccount(AuthMode.GUEST)

        assertEquals(UserProfile(name = "Ana", authMode = AuthMode.GUEST), repository.saved)
    }
}
