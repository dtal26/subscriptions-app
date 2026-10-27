package com.example.subscriptions

import com.example.subscriptions.data.local.BillingPeriod
import com.example.subscriptions.data.local.SubscriptionEntity
import com.example.subscriptions.util.PaymentUrgency
import com.example.subscriptions.util.daysUntilPayment
import com.example.subscriptions.util.daysUntilText
import com.example.subscriptions.util.formatMoney
import com.example.subscriptions.util.monthlyPrice
import com.example.subscriptions.util.nextPaymentDate
import com.example.subscriptions.util.pluralDays
import com.example.subscriptions.util.urgencyFor
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** Модульные тесты расчётов: сроки, склонения, суммы */
class SubscriptionUtilsTest {

    private fun subscription(price: Double, period: BillingPeriod, nextPayment: LocalDate) =
        SubscriptionEntity(
            userId = 1,
            name = "Тест",
            price = price,
            period = period,
            nextPaymentEpochDay = nextPayment.toEpochDay()
        )

    @Test
    fun pluralDays_correctEndings() {
        assertEquals("день", pluralDays(1))
        assertEquals("дня", pluralDays(2))
        assertEquals("дней", pluralDays(5))
        assertEquals("дней", pluralDays(11))
        assertEquals("дней", pluralDays(14))
        assertEquals("день", pluralDays(21))
        assertEquals("дня", pluralDays(22))
    }

    @Test
    fun daysUntilText_correctText() {
        assertEquals("сегодня", daysUntilText(0))
        assertEquals("завтра", daysUntilText(1))
        assertEquals("через 2 дня", daysUntilText(2))
        assertEquals("через 12 дней", daysUntilText(12))
    }

    @Test
    fun urgency_dependsOnDaysLeft() {
        assertEquals(PaymentUrgency.URGENT, urgencyFor(3))
        assertEquals(PaymentUrgency.SOON, urgencyFor(4))
        assertEquals(PaymentUrgency.SOON, urgencyFor(7))
        assertEquals(PaymentUrgency.NORMAL, urgencyFor(8))
    }

    @Test
    fun monthlyPrice_convertsPeriods() {
        val today = LocalDate.of(2026, 10, 19)
        assertEquals(399.0, subscription(399.0, BillingPeriod.MONTH, today).monthlyPrice(), 0.001)
        assertEquals(100.0, subscription(1200.0, BillingPeriod.YEAR, today).monthlyPrice(), 0.001)
        assertEquals(520.0, subscription(120.0, BillingPeriod.WEEK, today).monthlyPrice(), 0.001)
    }

    @Test
    fun pastPaymentDate_movesToNextPeriod() {
        val today = LocalDate.of(2026, 10, 19)
        val subscription = subscription(399.0, BillingPeriod.MONTH, LocalDate.of(2026, 10, 1))
        assertEquals(LocalDate.of(2026, 11, 1), subscription.nextPaymentDate(today))
        assertEquals(13L, subscription.daysUntilPayment(today))
    }

    @Test
    fun futurePaymentDate_staysTheSame() {
        val today = LocalDate.of(2026, 10, 19)
        val subscription = subscription(399.0, BillingPeriod.MONTH, LocalDate.of(2026, 10, 21))
        assertEquals(LocalDate.of(2026, 10, 21), subscription.nextPaymentDate(today))
        assertEquals(2L, subscription.daysUntilPayment(today))
    }

    @Test
    fun formatMoney_spacesAndComma() {
        assertEquals("2 436 ₽", formatMoney(2436.0))
        assertEquals("99,90 ₽", formatMoney(99.9))
        assertEquals("29 232 ₽", formatMoney(29232.0))
    }
}
