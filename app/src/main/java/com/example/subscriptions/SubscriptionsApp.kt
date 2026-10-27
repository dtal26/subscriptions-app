package com.example.subscriptions

import android.app.Application
import android.content.Context
import com.example.subscriptions.data.local.AppDatabase
import com.example.subscriptions.data.remote.NetworkModule
import com.example.subscriptions.data.repository.AuthRepository
import com.example.subscriptions.data.repository.CurrencyRepository
import com.example.subscriptions.data.repository.SubscriptionRepository
import com.example.subscriptions.data.session.SessionManager
import com.example.subscriptions.notifications.ReminderChecker
import com.example.subscriptions.notifications.ReminderNotifier
import com.example.subscriptions.notifications.ReminderWorker

/**
 * Класс приложения. Создаётся один раз при запуске
 * и хранит общие объекты (базу данных, репозитории).
 */
class SubscriptionsApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderNotifier.createChannel(this)  // канал уведомлений
        ReminderWorker.schedule(this)         // ежедневная проверка списаний
    }
}

/** Контейнер зависимостей: здесь создаются все репозитории */
class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)

    val session = SessionManager(context)
    val authRepository = AuthRepository(database.userDao(), session)
    val subscriptionRepository = SubscriptionRepository(database.subscriptionDao())
    val currencyRepository = CurrencyRepository(context, NetworkModule.createCbrApi())
    val reminderChecker = ReminderChecker(context, session, subscriptionRepository)
}
