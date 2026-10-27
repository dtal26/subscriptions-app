package com.example.subscriptions.util

import com.example.subscriptions.data.local.BillingPeriod
import com.example.subscriptions.data.local.SubscriptionEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/** Насколько срочно списание — от этого зависит цвет таблички на карточке */
enum class PaymentUrgency { URGENT, SOON, NORMAL }

/**
 * Ближайшая дата списания.
 * Если сохранённая дата уже прошла, она сдвигается на период вперёд
 * (подписка продлилась автоматически).
 */
fun SubscriptionEntity.nextPaymentDate(today: LocalDate = LocalDate.now()): LocalDate {
    var date = LocalDate.ofEpochDay(nextPaymentEpochDay)
    while (date.isBefore(today)) {
        date = when (period) {
            BillingPeriod.WEEK -> date.plusWeeks(1)
            BillingPeriod.MONTH -> date.plusMonths(1)
            BillingPeriod.YEAR -> date.plusYears(1)
        }
    }
    return date
}

/** Сколько дней осталось до списания */
fun SubscriptionEntity.daysUntilPayment(today: LocalDate = LocalDate.now()): Long =
    ChronoUnit.DAYS.between(today, nextPaymentDate(today))

/** Стоимость в пересчёте на месяц (для общей суммы и статистики) */
fun SubscriptionEntity.monthlyPrice(): Double = when (period) {
    BillingPeriod.WEEK -> price * 52 / 12
    BillingPeriod.MONTH -> price
    BillingPeriod.YEAR -> price / 12
}

/** Срочность по количеству дней: до 3 — срочно, до 7 — скоро, иначе — обычно */
fun urgencyFor(days: Long): PaymentUrgency = when {
    days <= 3 -> PaymentUrgency.URGENT
    days <= 7 -> PaymentUrgency.SOON
    else -> PaymentUrgency.NORMAL
}

/** Правильное окончание слова «день»: 1 день, 2 дня, 5 дней, 11 дней */
fun pluralDays(n: Long): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod10 == 1L && mod100 != 11L -> "день"
        mod10 in 2..4 && mod100 !in 12..14 -> "дня"
        else -> "дней"
    }
}

/** Текст для таблички: «сегодня», «завтра», «через 5 дней» */
fun daysUntilText(days: Long): String = when (days) {
    0L -> "сегодня"
    1L -> "завтра"
    else -> "через $days ${pluralDays(days)}"
}

/** Сумма в рублях с пробелами между тысячами: 2 436 ₽, 99,90 ₽ */
fun formatMoney(amount: Double): String {
    val rounded = Math.round(amount * 100) / 100.0
    val text = if (rounded % 1.0 == 0.0) {
        String.format(Locale.US, "%,d", rounded.toLong()).replace(',', ' ')
    } else {
        String.format(Locale.US, "%,.2f", rounded).replace(',', ' ').replace('.', ',')
    }
    return "$text ₽"
}

/** Сумма в целых рублях — для итогов и статистики */
fun formatRub(amount: Double): String = formatMoney(Math.round(amount).toDouble())

/** Курс с двумя знаками после запятой: 81,25 */
fun formatRate(rate: Double): String = String.format(Locale.US, "%.2f", rate).replace('.', ',')

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

/** Дата в формате 17.10.2026 */
fun formatDate(date: LocalDate): String = date.format(dateFormatter)
