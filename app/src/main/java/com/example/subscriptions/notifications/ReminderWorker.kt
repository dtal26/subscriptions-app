package com.example.subscriptions.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.subscriptions.SubscriptionsApp
import java.util.concurrent.TimeUnit

/** Фоновая задача WorkManager: раз в сутки проверяет предстоящие списания */
class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as SubscriptionsApp
        app.container.reminderChecker.check(manual = false)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "payment_reminders"

        /** Регистрация ежедневной задачи (повторный вызов не создаёт дубликат) */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
