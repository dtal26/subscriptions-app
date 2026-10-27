package com.example.subscriptions.ui.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscriptions.data.local.BillingPeriod
import com.example.subscriptions.data.local.Category
import com.example.subscriptions.data.local.SubscriptionEntity
import com.example.subscriptions.data.repository.AuthRepository
import com.example.subscriptions.data.repository.SubscriptionRepository
import com.example.subscriptions.util.Validators
import com.example.subscriptions.util.nextPaymentDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Состояние экрана добавления / редактирования */
data class AddEditUiState(
    val isEdit: Boolean = false,
    val isLoading: Boolean = false,
    val name: String = "",
    val price: String = "",
    val period: BillingPeriod = BillingPeriod.MONTH,
    val category: Category = Category.ENTERTAINMENT,
    val nextPaymentDate: LocalDate = LocalDate.now().plusMonths(1),
    val reminderEnabled: Boolean = true,
    val nameError: String? = null,
    val priceError: String? = null,
    val isSaving: Boolean = false
)

class AddEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    /** id из маршрута: 0 — новая подписка, иначе — редактирование */
    private val subscriptionId: Long = savedStateHandle.get<Long>("id") ?: 0L
    private var original: SubscriptionEntity? = null

    var state by mutableStateOf(
        AddEditUiState(isEdit = subscriptionId != 0L, isLoading = subscriptionId != 0L)
    )
        private set

    init {
        if (subscriptionId != 0L) {
            viewModelScope.launch {
                val subscription = subscriptionRepository.get(subscriptionId)
                original = subscription
                state = if (subscription != null) {
                    state.copy(
                        isLoading = false,
                        name = subscription.name,
                        price = priceToText(subscription.price),
                        period = subscription.period,
                        category = subscription.category,
                        nextPaymentDate = subscription.nextPaymentDate(),
                        reminderEnabled = subscription.reminderEnabled
                    )
                } else {
                    state.copy(isLoading = false)
                }
            }
        }
    }

    fun onNameChange(value: String) {
        state = state.copy(name = value, nameError = null)
    }

    fun onPriceChange(value: String) {
        state = state.copy(price = value, priceError = null)
    }

    fun onPeriodChange(value: BillingPeriod) {
        state = state.copy(period = value)
    }

    fun onCategoryChange(value: Category) {
        state = state.copy(category = value)
    }

    fun onDateChange(value: LocalDate) {
        state = state.copy(nextPaymentDate = value)
    }

    fun onReminderChange(value: Boolean) {
        state = state.copy(reminderEnabled = value)
    }

    fun save(onDone: () -> Unit) {
        val nameError = Validators.validateSubscriptionName(state.name)
        val priceError = Validators.validatePrice(state.price)
        if (nameError != null || priceError != null) {
            state = state.copy(nameError = nameError, priceError = priceError)
            return
        }

        state = state.copy(isSaving = true)
        viewModelScope.launch {
            val userId = authRepository.currentUserId.first() ?: return@launch
            val base = original ?: SubscriptionEntity(
                userId = userId,
                name = "",
                price = 0.0,
                nextPaymentEpochDay = 0
            )
            subscriptionRepository.save(
                base.copy(
                    name = state.name.trim(),
                    price = state.price.replace(',', '.').toDouble(),
                    currency = "RUB",
                    period = state.period,
                    category = state.category,
                    nextPaymentEpochDay = state.nextPaymentDate.toEpochDay(),
                    reminderEnabled = state.reminderEnabled
                )
            )
            onDone()
        }
    }

    /** 399.0 → «399», 9.99 → «9.99» */
    private fun priceToText(price: Double): String =
        if (price % 1.0 == 0.0) price.toLong().toString() else price.toString()
}
