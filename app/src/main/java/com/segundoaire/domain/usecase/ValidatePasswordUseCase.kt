package com.segundoaire.domain.usecase

import javax.inject.Inject

/** Lo que le falta a una contraseña, de lo más básico a lo más específico. */
enum class PasswordError { TOO_SHORT, MISSING_LETTER, MISSING_NUMBER }

/**
 * Valida una contraseña: [MIN_LENGTH] caracteres o más, con al menos una letra
 * y un número. Devuelve lo primero que falta, o `null` si es válida.
 */
class ValidatePasswordUseCase @Inject constructor() {
    operator fun invoke(password: String): PasswordError? = when {
        password.length < MIN_LENGTH -> PasswordError.TOO_SHORT
        password.none(Char::isLetter) -> PasswordError.MISSING_LETTER
        password.none(Char::isDigit) -> PasswordError.MISSING_NUMBER
        else -> null
    }

    companion object {
        const val MIN_LENGTH = 8
    }
}
