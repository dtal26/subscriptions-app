package com.example.subscriptions.util

/**
 * Проверка полей форм входа, регистрации и добавления подписки.
 * Каждая функция возвращает текст ошибки или null, если поле заполнено верно.
 */
object Validators {

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    const val MIN_PASSWORD_LENGTH = 8

    fun validateName(name: String): String? = when {
        name.isBlank() -> "Введите имя"
        name.trim().length < 2 -> "Имя слишком короткое"
        else -> null
    }

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Введите email"
        !emailRegex.matches(email.trim()) -> "Неверный формат email"
        else -> null
    }

    fun validatePassword(password: String): String? = when {
        password.isEmpty() -> "Введите пароль"
        password.length < MIN_PASSWORD_LENGTH -> "Минимум $MIN_PASSWORD_LENGTH символов"
        else -> null
    }

    fun validatePasswordRepeat(password: String, repeat: String): String? = when {
        repeat.isEmpty() -> "Повторите пароль"
        password != repeat -> "Пароли не совпадают"
        else -> null
    }

    fun validateSubscriptionName(name: String): String? =
        if (name.isBlank()) "Введите название" else null

    fun validatePrice(price: String): String? {
        val value = price.replace(',', '.').toDoubleOrNull()
        return when {
            price.isBlank() -> "Введите стоимость"
            value == null -> "Введите число"
            value <= 0 -> "Стоимость должна быть больше 0"
            else -> null
        }
    }
}
