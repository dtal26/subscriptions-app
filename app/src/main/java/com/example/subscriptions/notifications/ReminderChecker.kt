package com.example.subscriptions.notifications

import android.content.Context
import com.example.subscriptions.data.repository.SubscriptionRepository
import com.example.subscriptions.data.session.SessionManager
import com.example.subscriptions.util.daysUntilPayment
import com.example.subscriptions.util.daysUntilText
import com.example.subscriptions.util.formatMoney
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Проверяет подписки и отправляет уведомления о предстоящих списаниях.
 * Используется фоновой задачей (раз в день) и кнопкой «Проверить сейчас» в настройках.
 */
class ReminderChecker(
    private val context: Context,
    private val session: SessionManager,
    private val subscriptionRepository: SubscriptionRepository
) {
    /**
     * @param manual false — ежедневная проверка: уведомление приходит ровно
     *               за выбранное число дней до списания;
     *               true — ручная проверка: показываются все списания
     *               в пределах выбранного срока.
     * @return сколько уведомлений отправлено
     */
    suspend fun check(manual: Boolean): Int {
        val userId = session.currentUserId.first() ?: return 0
        if (!session.remindersEnabled.first()) return 0
        val remindDays = session.remindDaysBefore.first().toLong()
        val today = LocalDate.now()

        val due = subscriptionRepository.observeAll(userId).first()
            .filter { it.reminderEnabled }
            .map { it to it.daysUntilPayment(today) }
            .filter { (_, days) -> if (manual) days <= remindDays else days == remindDays }

        var sent = 0
        due.forEach { (subscription, days) ->
            val shown = ReminderNotifier.show(
                context = context,
                id = subscription.id.toInt(),
                title = "Скоро списание: ${subscription.name}",
                text = "${formatMoney(subscription.price)} спишется ${daysUntilText(days)}"
            )
            if (shown) sent++
        }
        return sent
    }
}
