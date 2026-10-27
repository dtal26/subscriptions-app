package com.example.subscriptions.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscriptions.data.repository.AuthRepository
import com.example.subscriptions.data.session.SessionManager
import com.example.subscriptions.notifications.ReminderChecker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Состояние экрана настроек */
data class SettingsUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val userEmail: String = "",
    val remindersEnabled: Boolean = true,
    val remindDaysBefore: Int = 1
)

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val session: SessionManager,
    private val reminderChecker: ReminderChecker
) : ViewModel() {

    val state: StateFlow<SettingsUiState> = authRepository.currentUserId
        .filterNotNull()
        .flatMapLatest { userId ->
            combine(
                authRepository.observeUser(userId),
                session.remindersEnabled,
                session.remindDaysBefore
            ) { user, enabled, days ->
                SettingsUiState(
                    isLoading = false,
                    userName = user?.name.orEmpty(),
                    userEmail = user?.email.orEmpty(),
                    remindersEnabled = enabled,
                    remindDaysBefore = days
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { session.setRemindersEnabled(enabled) }
    }

    fun setRemindDaysBefore(days: Int) {
        viewModelScope.launch { session.setRemindDaysBefore(days) }
    }

    /** Ручная проверка напоминаний; результат — число отправленных уведомлений */
    fun checkRemindersNow(onResult: (Int) -> Unit) {
        viewModelScope.launch { onResult(reminderChecker.check(manual = true)) }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }
}
