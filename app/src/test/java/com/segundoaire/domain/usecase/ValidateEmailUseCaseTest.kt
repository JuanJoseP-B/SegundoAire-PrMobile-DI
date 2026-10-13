package com.segundoaire.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateEmailUseCaseTest {

    private val validateEmail = ValidateEmailUseCase()

    @Test
    fun `acepta correos con formato valido`() {
        listOf(
            "ana@correo.com",
            "ana.maria@correo.com.co",
            "ana+apps@mi-correo.org",
            "a_b%c@sub.dominio.io",
            "ANA@CORREO.COM"
        ).forEach { email ->
            assertTrue(email, validateEmail(email))
        }
    }

    @Test
    fun `ignora los espacios de los extremos`() {
        assertTrue(validateEmail("  ana@correo.com  "))
    }

    @Test
    fun `rechaza correos incompletos o mal formados`() {
        listOf(
            "",
            "   ",
            "ana",
            "ana@",
            "@correo.com",
            "ana@correo",
            "ana@correo.c",
            "ana@.com",
            "ana@correo..com",
            "ana..maria@correo.com",
            ".ana@correo.com",
            "ana.@correo.com",
            "ana@-correo.com",
            "ana maria@correo.com",
            "ana@correo .com",
            "ana@@correo.com",
            "ana@correo.com."
        ).forEach { email ->
            assertFalse(email, validateEmail(email))
        }
    }

    @Test
    fun `rechaza correos mas largos que el limite`() {
        val email = "a".repeat(250) + "@correo.com"

        assertFalse(validateEmail(email))
    }
}
