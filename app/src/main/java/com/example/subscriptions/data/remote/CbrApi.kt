package com.example.subscriptions.data.remote

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/**
 * REST API с курсами валют Центрального банка РФ.
 * Адрес: https://www.cbr-xml-daily.ru/daily_json.js
 */
interface CbrApi {
    @GET("daily_json.js")
    suspend fun getDailyRates(): CbrResponse
}

/** Ответ сервера. @Keep — чтобы обфускация R8 не переименовала поля, нужные Gson */
@Keep
data class CbrResponse(
    @SerializedName("Date") val date: String,
    @SerializedName("Valute") val valute: Map<String, CbrCurrency>
)

@Keep
data class CbrCurrency(
    @SerializedName("CharCode") val charCode: String,
    @SerializedName("Nominal") val nominal: Int,
    @SerializedName("Value") val value: Double
)

/** Создание клиента Retrofit */
object NetworkModule {
    private const val BASE_URL = "https://www.cbr-xml-daily.ru/"

    fun createCbrApi(): CbrApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CbrApi::class.java)
}
