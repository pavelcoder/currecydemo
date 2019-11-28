package ru.pavelcoder.modulbankdemo.dagger

import android.content.Context
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.pavelcoder.modulbankdemo.BuildConfig
import ru.pavelcoder.modulbankdemo.logger.Logger
import ru.pavelcoder.modulbankdemo.logger.LoggerImpl
import ru.pavelcoder.modulbankdemo.model.bank.Bank
import ru.pavelcoder.modulbankdemo.model.bank.LocalBankFactory
import ru.pavelcoder.modulbankdemo.model.currencyrates.CurrencyRatesFetcher
import ru.pavelcoder.modulbankdemo.model.currencyrates.CurrencyRatesSource
import ru.pavelcoder.modulbankdemo.model.db.MoneyRealmRepository
import ru.pavelcoder.modulbankdemo.model.db.MoneyRepository
import ru.pavelcoder.modulbankdemo.model.retrofit.ExchangeService
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
open class DaggerModule(
    private val applicationContext: Context
) {
    companion object {
        const val CURRENCY_RATES_HOST = "CURRENCY_RATES_HOST"
        private const val REALM_DB_NAME = "realm.currencies.db"
    }

    @Provides
    @Named(CURRENCY_RATES_HOST)
    fun providesCurrencyRatesHost() = "https://api.exchangeratesapi.io/"

    @Provides
    @Singleton
    fun providesCurrencyRatesService(@Named(CURRENCY_RATES_HOST) host: String): ExchangeService {
        val timeoutMs = 10_000L
        val okHttpClient = OkHttpClient.Builder()
            .callTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(host)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ExchangeService::class.java)
    }

    @Provides
    @Singleton
    fun providesCurrencyRatesFetcher(service: ExchangeService, logger: Logger) = CurrencyRatesFetcher(service, logger)

    @Provides
    fun providesContext() = applicationContext

    @Provides
    fun providesLogger(): Logger {
        return LoggerImpl( ! BuildConfig.DEBUG )
    }

    @Singleton
    @Provides
    fun providesCurrencyRepository(context: Context): MoneyRepository {
        Realm.init(context)
        val realmConfig = RealmConfiguration.Builder()
            .name(REALM_DB_NAME)
            .build()
        val realm = Realm.getInstance(realmConfig)
        return MoneyRealmRepository(realm)
    }

    @Singleton
    @Provides
    fun providesBank(ratesFetcher: CurrencyRatesFetcher, moneyRepository: MoneyRepository): Bank {
        return LocalBankFactory.createBank(ratesFetcher, moneyRepository)
    }
}
