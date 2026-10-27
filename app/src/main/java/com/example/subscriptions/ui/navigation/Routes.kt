package com.example.subscriptions.ui.navigation

/** Адреса (маршруты) экранов приложения */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val STATS = "stats"
    const val SETTINGS = "settings"

    /** Добавление (id = 0) или редактирование подписки */
    const val ADD_EDIT = "edit?id={id}"
    fun addEdit(id: Long = 0L) = "edit?id=$id"

    /** Подробная информация о подписке */
    const val DETAILS = "details/{id}"
    fun details(id: Long) = "details/$id"

    /** Экраны, на которых показывается нижнее меню */
    val tabs = setOf(HOME, STATS, SETTINGS)
}
