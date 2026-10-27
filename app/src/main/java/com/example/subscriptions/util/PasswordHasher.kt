package com.example.subscriptions.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Хэширование паролей.
 * Пароль никогда не хранится в открытом виде: в базу записываются
 * случайная «соль» и хэш PBKDF2 (HMAC-SHA256, 100 000 итераций).
 */
object PasswordHasher {

    private const val ITERATIONS = 100_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16

    /** Случайная соль для нового пользователя */
    fun generateSalt(): String {
        val bytes = ByteArray(SALT_LENGTH_BYTES)
        SecureRandom().nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    /** Хэш пароля с заданной солью */
    fun hash(password: String, salt: String): String {
        val spec = PBEKeySpec(
            password.toCharArray(),
            Base64.getDecoder().decode(salt),
            ITERATIONS,
            KEY_LENGTH_BITS
        )
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(hash)
    }

    /** Проверка введённого пароля */
    fun verify(password: String, salt: String, expectedHash: String): Boolean {
        val actual = hash(password, salt)
        return MessageDigest.isEqual(actual.toByteArray(), expectedHash.toByteArray())
    }
}
