package com.segundoaire.domain.usecase

import javax.inject.Inject

/**
 * Valida el formato de un correo: usuario, arroba y un dominio con al menos un
 * punto y una terminación de dos letras o más. Los espacios de los extremos se
 * ignoran. No comprueba que el correo exista: la cuenta es simulada.
 */
class ValidateEmailUseCase @Inject constructor() {
    operator fun invoke(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.length <= MAX_LENGTH && EMAIL_FORMAT.matches(trimmed)
    }

    private companion object {
        // Límite de una dirección según RFC 5321
        const val MAX_LENGTH = 254

        val EMAIL_FORMAT = Regex(
            "[A-Za-z0-9_%+-]+(\\.[A-Za-z0-9_%+-]+)*@[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?" +
                "(\\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,}"
        )
    }
}
