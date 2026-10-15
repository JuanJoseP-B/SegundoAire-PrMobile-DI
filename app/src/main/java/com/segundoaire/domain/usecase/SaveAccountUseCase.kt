package com.segundoaire.domain.usecase

import com.segundoaire.domain.model.AuthMode
import com.segundoaire.domain.repository.UserProfileRepository
import javax.inject.Inject

/**
 * Guarda la cuenta simulada. El correo se normaliza (sin espacios y en
 * minúsculas) y solo se conserva con [AuthMode.EMAIL]: los demás modos no
 * tienen un correo real detrás.
 */
class SaveAccountUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(authMode: AuthMode, email: String = "") = repository.saveAccount(
        authMode = authMode,
        email = if (authMode == AuthMode.EMAIL) email.trim().lowercase() else ""
    )
}
