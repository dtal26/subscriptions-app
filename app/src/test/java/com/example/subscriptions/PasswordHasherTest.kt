package com.example.subscriptions

import com.example.subscriptions.util.PasswordHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Модульные тесты хэширования паролей */
class PasswordHasherTest {

    private val password = "MySecret123"

    @Test
    fun correctPassword_verified() {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(password, salt)
        assertTrue(PasswordHasher.verify(password, salt, hash))
    }

    @Test
    fun wrongPassword_rejected() {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(password, salt)
        assertFalse(PasswordHasher.verify("WrongPassword", salt, hash))
    }

    @Test
    fun sameInput_sameHash() {
        val salt = PasswordHasher.generateSalt()
        assertEquals(PasswordHasher.hash(password, salt), PasswordHasher.hash(password, salt))
    }

    @Test
    fun differentSalts_differentHashes() {
        // Одинаковые пароли разных пользователей дают разные хэши
        val hash1 = PasswordHasher.hash(password, PasswordHasher.generateSalt())
        val hash2 = PasswordHasher.hash(password, PasswordHasher.generateSalt())
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun hash_doesNotContainPassword() {
        val hash = PasswordHasher.hash(password, PasswordHasher.generateSalt())
        assertFalse(hash.contains(password))
    }
}
