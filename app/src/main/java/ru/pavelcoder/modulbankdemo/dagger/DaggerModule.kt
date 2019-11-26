package ru.pavelcoder.modulbankdemo.dagger

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.pavelcoder.modulbankdemo.model.currencyrates.CurrencyRatesFetcher
import ru.pavelcoder.modulbankdemo.model.retrofit.ExchangeService
import javax.inject.Named
import javax.inject.Singleton

@Module
open class DaggerModule {
    companion object {
        const val CURRENCY_RATES_HOST = "CURRENCY_RATES_HOST"
    }

    @Provides
    @Named(CURRENCY_RATES_HOST)
    fun providesCurrencyRatesHost() = "https://api.exchangeratesapi.io/"

    @Provides
    @Singleton
    fun providesCurrencyRatesService(@Named(CURRENCY_RATES_HOST) host: String): ExchangeService {
        return Retrofit.Builder()
            .baseUrl(host)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeService::class.java)
    }

    @Provides
    @Singleton
    fun providesCurrencyRatesFetcher(service: ExchangeService) =
        CurrencyRatesFetcher(service)
}
