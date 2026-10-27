package com.example.subscriptions

import com.example.subscriptions.util.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Модульные тесты проверки полей форм */
class ValidatorsTest {

    @Test
    fun correctEmail_noError() {
        assertNull(Validators.validateEmail("daria@mail.ru"))
    }

    @Test
    fun emailWithoutAt_error() {
        assertEquals("Неверный формат email", Validators.validateEmail("daria.mail.ru"))
    }

    @Test
    fun emptyEmail_error() {
        assertEquals("Введите email", Validators.validateEmail(""))
    }

    @Test
    fun shortPassword_error() {
        assertEquals("Минимум 8 символов", Validators.validatePassword("1234567"))
    }

    @Test
    fun passwordOfEightChars_noError() {
        assertNull(Validators.validatePassword("12345678"))
    }

    @Test
    fun differentPasswords_error() {
        assertEquals(
            "Пароли не совпадают",
            Validators.validatePasswordRepeat("12345678", "87654321")
        )
    }

    @Test
    fun samePasswords_noError() {
        assertNull(Validators.validatePasswordRepeat("12345678", "12345678"))
    }

    @Test
    fun oneLetterName_error() {
        assertEquals("Имя слишком короткое", Validators.validateName("Д"))
    }

    @Test
    fun price_validation() {
        assertNull(Validators.validatePrice("399"))
        assertNull(Validators.validatePrice("99,90")) // запятая тоже допускается
        assertEquals("Введите число", Validators.validatePrice("abc"))
        assertNotNull(Validators.validatePrice("0"))
        assertNotNull(Validators.validatePrice(""))
    }
}
