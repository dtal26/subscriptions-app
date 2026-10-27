package com.example.subscriptions.data.repository

import com.example.subscriptions.data.local.UserDao
import com.example.subscriptions.data.local.UserEntity
import com.example.subscriptions.data.session.SessionManager
import com.example.subscriptions.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/** Результат входа или регистрации */
sealed class AuthResult {
    data class Success(val userId: Long) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/** Регистрация, вход и выход пользователя */
class AuthRepository(
    private val userDao: UserDao,
    private val session: SessionManager
) {
    val currentUserId: Flow<Long?> = session.currentUserId

    fun observeUser(id: Long): Flow<UserEntity?> = userDao.observeById(id)

    suspend fun register(name: String, email: String, password: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        if (userDao.findByEmail(normalizedEmail) != null) {
            return AuthResult.Error("Пользователь с таким email уже зарегистрирован")
        }
        // Хэширование — долгая операция, выполняем её не в главном потоке
        val (salt, hash) = withContext(Dispatchers.Default) {
            val salt = PasswordHasher.generateSalt()
            salt to PasswordHasher.hash(password, salt)
        }
        val id = userDao.insert(
            UserEntity(
                name = name.trim(),
                email = normalizedEmail,
                passwordHash = hash,
                salt = salt
            )
        )
        session.login(id)
        return AuthResult.Success(id)
    }

    suspend fun login(email: String, password: String): AuthResult {
        val user = userDao.findByEmail(email.trim().lowercase())
            ?: return AuthResult.Error("Неверный email или пароль")
        val ok = withContext(Dispatchers.Default) {
            PasswordHasher.verify(password, user.salt, user.passwordHash)
        }
        if (!ok) return AuthResult.Error("Неверный email или пароль")
        session.login(user.id)
        return AuthResult.Success(user.id)
    }

    suspend fun logout() = session.logout()
}
