package com.example.subscriptions.data.repository

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.subscriptions.data.remote.CbrApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.OffsetDateTime

/** Курсы валют: сколько рублей стоит 1 доллар и 1 евро, и на какую дату */
data class CurrencyRates(
    val usd: Double,
    val eur: Double,
    val date: LocalDate
)

private val Context.currencyStore by preferencesDataStore(name = "currency")

/**
 * Загружает курсы валют из сети и сохраняет их в DataStore.
 * Если интернета нет, используется последний сохранённый курс.
 */
class CurrencyRepository(
    private val context: Context,
    private val api: CbrApi
) {
    private val keyUsd = doublePreferencesKey("usd")
    private val keyEur = doublePreferencesKey("eur")
    private val keyDate = longPreferencesKey("date")

    /** Последний сохранённый курс или null, если курс ещё ни разу не загружался */
    val rates: Flow<CurrencyRates?> = context.currencyStore.data.map { prefs ->
        val usd = prefs[keyUsd]
        val eur = prefs[keyEur]
        val day = prefs[keyDate]
        if (usd != null && eur != null && day != null) {
            CurrencyRates(usd, eur, LocalDate.ofEpochDay(day))
        } else {
            null
        }
    }

    /** Загрузка свежего курса. Возвращает true при успехе, false — если нет сети или ошибка */
    suspend fun refresh(): Boolean = try {
        val response = api.getDailyRates()
        val usd = response.valute.getValue("USD").let { it.value / it.nominal }
        val eur = response.valute.getValue("EUR").let { it.value / it.nominal }
        val date = OffsetDateTime.parse(response.date).toLocalDate()
        context.currencyStore.edit {
            it[keyUsd] = usd
            it[keyEur] = eur
            it[keyDate] = date.toEpochDay()
        }
        true
    } catch (e: Exception) {
        false
    }
}
