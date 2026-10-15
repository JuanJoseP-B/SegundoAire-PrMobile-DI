package com.segundoaire.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatePasswordUseCaseTest {

    private val validatePassword = ValidatePasswordUseCase()

    @Test
    fun `acepta ocho caracteres o mas con letra y numero`() {
        listOf("abcdefg1", "1234567a", "Segundo4ire!", "contraseña2026").forEach { password ->
            assertNull(password, validatePassword(password))
        }
    }

    @Test
    fun `una contrasena corta marca el largo aunque tenga letra y numero`() {
        listOf("", "a1", "abcdef1").forEach { password ->
            assertEquals(password, PasswordError.TOO_SHORT, validatePassword(password))
        }
    }

    @Test
    fun `sin letras pide una letra`() {
        assertEquals(PasswordError.MISSING_LETTER, validatePassword("12345678"))
    }

    @Test
    fun `sin numeros pide un numero`() {
        assertEquals(PasswordError.MISSING_NUMBER, validatePassword("abcdefgh"))
    }

    @Test
    fun `los espacios cuentan como caracteres pero no como letra ni numero`() {
        assertEquals(PasswordError.MISSING_LETTER, validatePassword("1234 5678"))
    }
}
