package com.example.subscriptions.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscriptions.data.local.BillingPeriod
import com.example.subscriptions.data.local.Category
import com.example.subscriptions.data.local.SubscriptionEntity
import com.example.subscriptions.data.repository.AuthRepository
import com.example.subscriptions.data.repository.CurrencyRates
import com.example.subscriptions.data.repository.CurrencyRepository
import com.example.subscriptions.data.repository.SubscriptionRepository
import com.example.subscriptions.util.daysUntilPayment
import com.example.subscriptions.util.monthlyPrice
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Подписка в виде, удобном для отображения */
data class SubscriptionItem(
    val id: Long,
    val name: String,
    val category: Category,
    val price: Double,
    val period: BillingPeriod,
    val daysLeft: Long
)

/** Состояние загрузки курса валют */
enum class RatesStatus { LOADING, OK, FAILED }

/** Состояние главного экрана */
data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val items: List<SubscriptionItem> = emptyList(),
    /** Сумма расходов в месяц */
    val totalMonthly: Double = 0.0,
    /** Курс ЦБ — справочная информация, загружается из сети */
    val rates: CurrencyRates? = null,
    val ratesStatus: RatesStatus = RatesStatus.LOADING
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val ratesStatus = MutableStateFlow(RatesStatus.LOADING)

    init {
        refreshRates()
    }

    /** Загрузка курса валют из сети (при открытии экрана и по кнопке «Обновить») */
    fun refreshRates() {
        viewModelScope.launch {
            ratesStatus.value = RatesStatus.LOADING
            ratesStatus.value = if (currencyRepository.refresh()) RatesStatus.OK else RatesStatus.FAILED
        }
    }

    val state: StateFlow<HomeUiState> = authRepository.currentUserId
        .filterNotNull()
        .flatMapLatest { userId ->
            combine(
                authRepository.observeUser(userId),
                subscriptionRepository.observeAll(userId),
                currencyRepository.rates,
                ratesStatus
            ) { user, subscriptions, rates, status ->
                val today = LocalDate.now()
                HomeUiState(
                    isLoading = false,
                    userName = user?.name.orEmpty(),
                    items = subscriptions
                        .map {
                            SubscriptionItem(
                                id = it.id,
                                name = it.name,
                                category = it.category,
                                price = it.price,
                                period = it.period,
                                daysLeft = it.daysUntilPayment(today)
                            )
                        }
                        .sortedBy { it.daysLeft },
                    totalMonthly = subscriptions.sumOf { it.monthlyPrice() },
                    rates = rates,
                    ratesStatus = status
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    /** Добавляет примеры подписок — удобно для демонстрации */
    fun addDemoData() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId.first() ?: return@launch
            val today = LocalDate.now()
            data class Demo(val name: String, val price: Double, val category: Category, val days: Long)
            listOf(
                Demo("Яндекс Плюс", 399.0, Category.ENTERTAINMENT, 2),
                Demo("VK Музыка", 199.0, Category.MUSIC, 5),
                Demo("Okko", 449.0, Category.ENTERTAINMENT, 12),
                Demo("Notion", 990.0, Category.WORK, 18),
                Demo("Duolingo", 399.0, Category.EDUCATION, 25)
            ).forEach { d ->
                subscriptionRepository.save(
                    SubscriptionEntity(
                        userId = userId,
                        name = d.name,
                        price = d.price,
                        category = d.category,
                        nextPaymentEpochDay = today.plusDays(d.days).toEpochDay()
                    )
                )
            }
        }
    }
}
