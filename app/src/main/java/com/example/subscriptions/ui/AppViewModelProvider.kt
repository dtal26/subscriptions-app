package com.example.subscriptions.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.subscriptions.SubscriptionsApp
import com.example.subscriptions.ui.auth.LoginViewModel
import com.example.subscriptions.ui.auth.RegisterViewModel
import com.example.subscriptions.ui.details.DetailsViewModel
import com.example.subscriptions.ui.edit.AddEditViewModel
import com.example.subscriptions.ui.home.HomeViewModel
import com.example.subscriptions.ui.settings.SettingsViewModel
import com.example.subscriptions.ui.stats.StatsViewModel

/**
 * Фабрика ViewModel: создаёт ViewModel и передаёт им репозитории
 * из контейнера зависимостей (SubscriptionsApp.container).
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { LoginViewModel(app().container.authRepository) }
        initializer { RegisterViewModel(app().container.authRepository) }
        initializer {
            HomeViewModel(
                app().container.authRepository,
                app().container.subscriptionRepository,
                app().container.currencyRepository
            )
        }
        initializer {
            StatsViewModel(
                app().container.authRepository,
                app().container.subscriptionRepository
            )
        }
        initializer {
            AddEditViewModel(
                createSavedStateHandle(),
                app().container.authRepository,
                app().container.subscriptionRepository
            )
        }
        initializer {
            DetailsViewModel(
                createSavedStateHandle(),
                app().container.subscriptionRepository
            )
        }
        initializer {
            SettingsViewModel(
                app().container.authRepository,
                app().container.session,
                app().container.reminderChecker
            )
        }
    }
}

private fun CreationExtras.app(): SubscriptionsApp =
    this[APPLICATION_KEY] as SubscriptionsApp
