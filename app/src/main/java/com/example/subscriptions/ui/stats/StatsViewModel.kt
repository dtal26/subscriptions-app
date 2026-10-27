package com.example.subscriptions.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscriptions.data.local.Category
import com.example.subscriptions.data.repository.AuthRepository
import com.example.subscriptions.data.repository.SubscriptionRepository
import com.example.subscriptions.util.monthlyPrice
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Расходы по одной категории */
data class CategoryStat(
    val category: Category,
    val monthlyAmount: Double,
    val fraction: Double // доля от общей суммы, от 0 до 1
)

/** Состояние экрана статистики */
data class StatsUiState(
    val isLoading: Boolean = true,
    val totalMonthly: Double = 0.0,
    val categories: List<CategoryStat> = emptyList(),
    val mostExpensiveName: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    authRepository: AuthRepository,
    subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    val state: StateFlow<StatsUiState> = authRepository.currentUserId
        .filterNotNull()
        .flatMapLatest { userId -> subscriptionRepository.observeAll(userId) }
        .map { subscriptions ->
            val total = subscriptions.sumOf { it.monthlyPrice() }
            val categories = subscriptions
                .groupBy { it.category }
                .map { (category, list) ->
                    val amount = list.sumOf { it.monthlyPrice() }
                    CategoryStat(
                        category = category,
                        monthlyAmount = amount,
                        fraction = if (total > 0) amount / total else 0.0
                    )
                }
                .sortedByDescending { it.monthlyAmount }

            StatsUiState(
                isLoading = false,
                totalMonthly = total,
                categories = categories,
                mostExpensiveName = subscriptions.maxByOrNull { it.monthlyPrice() }?.name
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())
}
