package com.example.subscriptions.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscriptions.data.local.SubscriptionEntity
import com.example.subscriptions.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Состояние экрана деталей */
data class DetailsUiState(
    val isLoading: Boolean = true,
    val subscription: SubscriptionEntity? = null
)

class DetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val subscriptionId: Long = savedStateHandle.get<Long>("id") ?: 0L

    /** Подписка из базы; после редактирования данные обновятся автоматически */
    val state: StateFlow<DetailsUiState> = repository.observe(subscriptionId)
        .map { DetailsUiState(isLoading = false, subscription = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailsUiState())

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.delete(subscriptionId)
            onDone()
        }
    }
}
