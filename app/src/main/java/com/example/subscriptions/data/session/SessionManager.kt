package com.example.subscriptions.data.session

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

/**
 * Хранит сессию (id вошедшего пользователя) и настройки в DataStore.
 * Благодаря этому после перезапуска приложения не нужно входить заново.
 */
class SessionManager(private val context: Context) {

    private val keyUserId = longPreferencesKey("user_id")
    private val keyRemindersEnabled = booleanPreferencesKey("reminders_enabled")
    private val keyRemindDays = intPreferencesKey("remind_days_before")

    /** id текущего пользователя или null, если никто не вошёл */
    val currentUserId: Flow<Long?> = context.dataStore.data.map { it[keyUserId] }

    /** Включены ли напоминания о списаниях (по умолчанию — да) */
    val remindersEnabled: Flow<Boolean> = context.dataStore.data.map { it[keyRemindersEnabled] ?: true }

    /** За сколько дней напоминать о списании (по умолчанию за 1 день) */
    val remindDaysBefore: Flow<Int> = context.dataStore.data.map { it[keyRemindDays] ?: 1 }

    suspend fun login(userId: Long) {
        context.dataStore.edit { it[keyUserId] = userId }
    }

    suspend fun logout() {
        context.dataStore.edit { it.remove(keyUserId) }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[keyRemindersEnabled] = enabled }
    }

    suspend fun setRemindDaysBefore(days: Int) {
        context.dataStore.edit { it[keyRemindDays] = days }
    }
}
