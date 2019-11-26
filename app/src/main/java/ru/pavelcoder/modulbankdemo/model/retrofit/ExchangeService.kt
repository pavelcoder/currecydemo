package ru.pavelcoder.modulbankdemo.model.retrofit

import retrofit2.Response
import retrofit2.http.GET

interface ExchangeService {
    @GET("latest")
    suspend fun currencyRates(): Response<CurrencyRatesResponse>
}